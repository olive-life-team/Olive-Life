## 🚨 Fetch Join + 영속성 컨텍스트로 인한 비관적 락 무력화

---

### 🔎 배경

장바구니 다중 결제 시나리오에서 여러 상품에 동시에 비관적 락을 적용하던 중 발생한 문제입니다.
N+1 문제를 방지하기 위해 Fetch Join을 사용했는데, 이것이 오히려 비관적 락을 무력화시키는 예상치 못한 문제를 발견했습니다.

---

### ⚠️ 문제 상황

재고가 5개인 한정판 상품을 100명의 유저가 장바구니에 담아두고 동시에 결제를 시도하는 테스트 환경에서,
비관적 락을 적용했음에도 불구하고 결제 성공 건수가 5건이 아닌 50건이 발생하는 초과 판매 문제가 발생했습니다.

#### 문제 재현 코드
```java
// N+1 방지를 위해 Fetch Join 사용
List<CartItem> cartItems = cartItemRepository.findByCartWithProduct(cart);

// 비관적 락으로 Product 조회
List<Product> products = productRepository.findAllByIdWithLock(productIds);

// 재고 차감
for (CartItem item : cartItems) {
    Product product = productMap.get(item.getProduct().getId());
    product.decreaseStock(item.getQuantity()); // 락이 무력화된 상태
}
```

---

### ⚠️ 원인 분석

문제의 원인은 JPA 영속성 컨텍스트의 **동일성 보장(Identity) 원칙**과 Fetch Join의 조합이었습니다.

**① Fetch Join으로 인한 1차 캐시 선점**
- `findByCartWithProduct()`로 CartItem 조회 시 연관된 Product(재고 5)가 각 스레드의 1차 캐시에 미리 저장됨

**② 영속성 컨텍스트의 동일성 보장으로 인한 캐시 우선 반환**
- 이후 `findAllByIdWithLock()`으로 비관적 락 쿼리(SELECT FOR UPDATE) 실행 시
  DB에서 최신 재고(예: 4)를 읽어왔지만
  JPA는 1차 캐시에 동일한 ID의 엔티티가 이미 존재하면
  DB에서 가져온 최신 데이터를 버리고 캐시의 과거 데이터(재고 5)를 반환

#### 원인과 관련된 코드
```java
// 문제의 핵심: Fetch Join으로 Product가 1차 캐시에 올라간 상태에서
// SELECT FOR UPDATE를 실행해도 캐시의 과거 데이터를 반환
@Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart = :cart")
List<CartItem> findByCartWithProduct(@Param("cart") Cart cart);

// 비관적 락 쿼리가 실행되지만 캐시에 막혀 최신 재고를 읽지 못함
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id IN :ids")
List<Product> findAllByIdWithLock(@Param("ids") List<Long> ids);
```

결과적으로 모든 스레드가 재고 5를 기준으로 -1을 계산하여 덮어쓰는
**갱신 손실(Lost Update)** 이 발생했습니다.

---

### 💡 해결 방식

Product를 1차 캐시에 올리지 않고, 비관적 락 쿼리를 통해서만 최신 데이터를 로딩하도록 변경했습니다.
Lazy Loading과 프록시의 특성을 활용해 N+1 문제도 함께 해결했습니다.

#### 해결 코드
```java
// Fetch Join 제거 → 순수 CartItem만 조회
// Product는 프록시 객체로 생성됨 (DB 조회 X)
List<CartItem> cartItems = cartItemRepository.findByCart(cart);

// 프록시 객체에서 FK(id)만 추출 → N+1 쿼리 미발생
List<Long> productIds = cartItems.stream()
        .map(item -> item.getProduct().getId()) // 프록시 초기화 없이 FK만 추출
        .sorted() // 데드락 방지를 위한 정렬
        .toList();

// 비관적 락으로 단 1번의 쿼리로 최신 Product 로딩
// 이 시점에 최초로 1차 캐시에 적재 → 항상 최신 재고 보장
List<Product> products = productRepository.findAllByIdWithLock(productIds);

Map<Long, Product> productMap = products.stream()
        .collect(Collectors.toMap(Product::getId, p -> p));

// Map에서 꺼낸 진짜 객체로 재고 차감
for (CartItem item : cartItems) {
    Product product = productMap.get(item.getProduct().getId());
    product.decreaseStock(item.getQuantity());
}
```

#### 해결 포인트 정리
- Fetch Join 제거로 Product가 1차 캐시에 선점되는 것을 방지
- 프록시 객체의 식별자(FK) 조회는 초기화 없이 가능하므로 N+1 미발생
- `findAllByIdWithLock()`이 최초 로딩이 되어 항상 최신 재고를 1차 캐시에 적재
- 비관적 락이 의도대로 동작하여 정합성 보장

---

### ✅ 결과

- 100명 동시 결제 시도 중 정확히 5명만 성공하고 95명은 실패
- 최종 재고 0으로 동시성 제어 완벽 성공
- N+1 문제도 해결 (CartItem + Product를 각각 1번의 쿼리로 처리)

---

### 📚 학습한 점

- 비관적 락을 적용할 때는 해당 엔티티가 영속성 컨텍스트에 **언제 최초로 로드되는지** 생명주기를 완벽히 통제해야 락이 무력화되지 않는다
- 무조건적인 Fetch Join이 정답이 아니며, 성능(N+1 방어)과 정합성(Lock)을 동시에 달성하려면 프록시 객체의 초기화 시점을 전략적으로 활용해야 한다
- JPA의 동일성 보장 원칙은 성능 최적화를 위한 기능이지만, 동시성 환경에서는 오히려 독이 될 수 있다

---

### ✍️ 한 줄 정리

> 비관적 락은 DB 레벨에서 동작하지만, JPA 영속성 컨텍스트의 1차 캐시가 살아있으면 무력화될 수 있다. 락의 대상이 되는 엔티티의 로딩 시점을 반드시 통제해야 한다.

---

## 🚨 다중 상품 락 획득 순서로 인한 데드락 방지

---

### 🔎 배경

장바구니에 담긴 여러 상품을 동시에 결제하는 시나리오에서
여러 상품에 비관적 락을 동시에 적용해야 하는 상황이 발생했습니다.
처음에는 단순히 락을 걸면 정합성이 보장될 것이라 생각했지만,
락 획득 순서에 따라 데드락이 발생할 수 있다는 것을 발견했습니다.

---

### ⚠️ 문제 상황

Thread A와 Thread B가 서로 다른 순서로 상품에 락을 획득하려 할 때
순환 대기가 발생하여 데드락이 생길 수 있는 구조였습니다.

#### 문제 재현 코드
```java
// 정렬 없이 락 획득 시도
List<CartItem> cartItems = cartItemRepository.findByCart(cart);
List<Long> productIds = cartItems.stream()
        .map(item -> item.getProduct().getId())
        // sorted() 없음 → 스레드마다 다른 순서로 락 획득 시도
        .toList();

List<Product> products = productRepository.findAllByIdWithLock(productIds);
```

```text
Thread A: 상품3(id=3) 락 획득 → 상품1(id=1) 락 시도 (대기 중)
Thread B: 상품1(id=1) 락 획득 → 상품3(id=3) 락 시도 (대기 중)
→ 서로 상대방이 가진 락을 기다리며 영원히 진행 불가 → 데드락!
```

---

### ⚠️ 원인 분석

데드락의 근본 원인은 **락의 종류**가 아니라 **순환 대기구조** 자체입니다.

비관적 락(DB Lock)이든 Redis 분산 락이든
두 개 이상의 락을 **서로 다른 순서**로 획득하는 순간 데드락이 발생할 수 있습니다.

#### 원인과 관련된 코드
```java
// 장바구니 상품 순서가 스레드마다 다를 수 있음
// Thread A의 cartItems: [상품3, 상품1]
// Thread B의 cartItems: [상품1, 상품3]
// → 각자 다른 순서로 락 획득 시도 → 순환 대기 → 데드락
List<Long> productIds = cartItems.stream()
        .map(item -> item.getProduct().getId())
        .toList(); // 정렬 없음
```

---

### 💡 해결 방식

모든 스레드가 항상 **productId 오름차순**으로 락을 획득하도록 강제하여
순환 대기 구조 자체가 발생하지 않도록 설계했습니다.

#### 해결 코드
```java
// 데드락 방지: productId 오름차순 정렬 후 락 획득
List<Long> productIds = cartItems.stream()
        .map(item -> item.getProduct().getId())
        .sorted() // ← id 오름차순 정렬로 락 획득 순서 통일
        .toList();

List<Product> products = productRepository.findAllByIdWithLock(productIds);
```

#### 해결 포인트 정리
- 모든 스레드가 항상 작은 id부터 락을 획득하도록 강제
- Thread B는 Thread A가 끝날 때까지 대기 → 순환 대기 구조 자체가 발생하지 않음
- 비관적 락뿐만 아니라 Redis 분산 락에서도 동일한 원칙 적용 가능
- 단 한 줄(sorted())로 데드락을 설계 단계에서 원천 방지

---

### ✅ 결과

- productId 정렬 적용 후 다중 상품 동시 결제 테스트에서 데드락 없이 정상 처리
- 모든 스레드가 동일한 순서로 락을 획득하여 순환 대기 구조 제거
- 정합성 보장 유지

---

### 📚 학습한 점

- 데드락은 비관적 락만의 문제가 아니다. Redis 분산 락도 두 개 이상을 순서 없이 획득하면 동일한 순환 대기가 발생한다
- 근본 원인은 락의 종류가 아닌 순환 대기 구조 자체이므로, 해결책도 락의 종류를 바꾸는 것이 아니라 설계 단계에서 순서를 강제하는 것이다
- 하나의 비즈니스 로직에서 두 개 이상의 락을 동시에 획득하는 설계는 최대한 피하고, 불가피하다면 항상 동일한 순서로 획득하도록 강제해야 한다

---

### ✍️ 한 줄 정리

> 데드락의 근본 원인은 락의 종류가 아닌 순환 대기 구조이며, sorted()로 락 획득 순서를 통일하는 것만으로도 설계 단계에서 데드락을 원천 방지할 수 있다.

---

## 🚨 락 획득 전 수량 체크로 인한 정합성 깨짐

---

### 🔎 배경

분산 락 Retry with Backoff 전략을 적용한 후
튜터님으로부터 "300명 대비 100개 쿠폰인데 소요 시간이 너무 길지 않냐"는 피드백을 받았습니다.
100개 소진 후에도 나머지 200명이 재시도를 반복하며 불필요한 부하를 주는 것이 문제였고,
이를 해결하기 위해 락 획득 전 수량을 먼저 체크하는 로직을 추가했습니다.

---

### ⚠️ 문제 상황

수량 체크 로직 추가 후 소요 시간은 단축됐지만
4~5번에 한 번씩 성공 건수가 101건으로 테스트가 실패하는 문제가 발생했습니다.

#### 문제 재현 코드
```java
while (retry < 8) {
    // 락 획득 전 수량 먼저 체크
    Coupon coupon = couponRepository.findById(couponId).orElseThrow(...);
    if (coupon.getQuantity() <= 0) {
        throw new CouponException(CouponErrorCode.COUPON_OUT_OF_STOCK);
    }

    // 락 획득 시도
    if (lockRedisRepository.tryLock(lockKey, lockValue, 2)) {
        try {
            return couponService.issueCoupon(memberId, couponId);
        } finally {
            lockRedisRepository.releaseLock(lockKey, lockValue);
        }
    }
    retry++;
    Thread.sleep(50);
}
```

```text
성공 건수: 101건 → 테스트 실패
```

---

### ⚠️ 원인 분석

수량 체크와 락 획득 사이에 간격이 존재하기 때문에
다른 스레드가 끼어드는 문제가 발생했습니다.

#### 원인과 관련된 코드
```text
쿠폰 수량 1개 남은 상황

Thread A: 수량 1 확인 → 통과
Thread B: 수량 1 확인 → 통과
(둘 다 락 밖에서 같은 데이터를 읽음)

→ Thread A: 락 획득 → issueCoupon 실행 → 수량 1→0 → 성공
→ Thread B: 락 획득 → issueCoupon 실행 시도
            → decreaseCouponQuantity에서 수량 0 확인
            → 예외 발생해야 하지만
              수량 체크는 이미 통과한 상태
→ 101번째 발급 시도 발생
```

락 밖에서의 수량 체크는 **정확한 시점의 데이터를 보장할 수 없으며**
락의 보호 범위 밖에서 실행되기 때문에 정합성 보장이 불가능합니다.

---

### 💡 해결 방식

락 밖의 수량 체크 로직을 제거하고
이미 락 안에서 실행되고 있던 `decreaseCouponQuantity` 내부 체크만 유지했습니다.

#### 해결 코드
```java
// 수량 체크 제거
while (retry < 8) {
    if (lockRedisRepository.tryLock(lockKey, lockValue, 2)) {
        try {
            // decreaseCouponQuantity 내부에서 수량 체크
            // 락 안에서 실행되므로 정합성 보장
            return couponService.issueCoupon(memberId, couponId);
        } finally {
            lockRedisRepository.releaseLock(lockKey, lockValue);
        }
    }
    retry++;
    Thread.sleep(50);
}
```

```java
// 락 안에서 실행되는 수량 체크 (정합성 보장)
public void decreaseCouponQuantity() {
    if (this.quantity <= 0) {
        throw new CouponException(CouponErrorCode.COUPON_OUT_OF_STOCK);
    }
    this.quantity--;
}
```

#### 해결 포인트 정리
- 수량 검증은 반드시 락 안에서 실행되어야 정합성 보장
- 락 밖 체크는 성능 개선 목적이었지만 오히려 정합성을 깨뜨림
- `decreaseCouponQuantity` 내부 체크가 진짜 정합성을 보장하는 유일한 검증 포인트
- 성능 개선은 재시도 간격 조정(70ms → 50ms)으로 대체

---

### ✅ 결과

- 락 밖 수량 체크 제거 후 성공 건수 100건으로 안정화
- 정합성과 성능 개선을 모두 달성

---

### 📚 학습한 점

- 락의 보호 범위 밖에서 실행되는 검증 로직은 정합성을 보장할 수 없다
- 성능 개선을 위한 코드가 오히려 정합성을 깨뜨릴 수 있으므로
  동시성 환경에서는 변경 사항이 락의 보호 범위 안에 있는지 반드시 확인해야 한다


---

### ✍️ 한 줄 정리

> 락의 보호 범위 밖에서 실행되는 수량 체크는 정합성을 보장할 수 없으며, 모든 검증 로직은 반드시 락 안에서 실행되어야 한다.
