## 캐시 구현, 생각대로 되지 않았던 것들

---

### 1. Redis 역직렬화 오류:  `LinkedHashMap cannot be cast to ProductSearchResult`

**문제 상황**  
v3 검색 API 호출 시 Redis에서 캐시된 값을 꺼내는 과정에서 아래 예외가 발생했습니다.

    java.lang.ClassCastException: class java.util.LinkedHashMap
    cannot be cast to class ProductSearchResult

**원인 분석**  
`GenericJackson2JsonRedisSerializer`를 사용하면 Redis에 저장된 JSON을 역직렬화할 때 구체적인 타입 정보가 없으면 Jackson이 기본적으로 `LinkedHashMap`으로 반환합니다.  
즉, Redis에서 꺼낸 값이 `ProductSearchResult`가 아닌 `LinkedHashMap`이므로 캐스팅에 실패한 것입니다.

**해결 방법**  
`Jackson2JsonRedisSerializer<ProductSearchResult>`를 사용하여 역직렬화 시 항상 `ProductSearchResult` 타입으로 변환되도록 명시했습니다.

    new Jackson2JsonRedisSerializer<>(objectMapper, ProductSearchResult.class)

이렇게 하면 Redis에서 꺼낸 JSON이 항상 지정된 타입으로 변환되므로 캐스팅 오류가 발생하지 않습니다.

---

### 2. `Page<T>` Redis 직렬화 불가 문제

**문제 상황**  
v3 구현 초기에 `Page<ProductSummaryResponse>`를 그대로 Redis에 캐시하려 했으나 역직렬화 단계에서 실패했습니다.

**원인 분석**  
`Page`는 인터페이스이고, 실제 구현체인 `PageImpl`은 Jackson이 역직렬화할 때 필요한 기본 생성자가 없습니다.  
또한 `Page` 내부에는 `Pageable`, `Sort` 등 복잡한 의존 객체가 포함되어 있어 JSON으로 완전하게 복원하기 어렵습니다.

**해결 방법**  
`Page`를 대체하는 `ProductSearchResult` record를 별도로 만들어 필요한 필드만 담아 직렬화 가능한 구체 타입으로 변환했습니다.

    public record ProductSearchResult(
        List<ProductSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {
        public static ProductSearchResult from(Page<ProductSummaryResponse> page) {
            return new ProductSearchResult(
                page.getContent(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages()
            );
        }
    }

---

### 3. v2와 v4 캐시 이름 충돌로 인한 `ClassCastException`

**문제 상황**  
v4 검색 API 호출 시 Caffeine 캐시에서 값을 꺼내는 과정에서 아래 예외가 발생했습니다.

    java.lang.ClassCastException: class org.springframework.data.domain.PageImpl
    cannot be cast to class ProductSearchResult

**원인 분석**  
v2(`@Cacheable`)는 `Page<ProductSummaryResponse>`를 캐시에 저장하고,  
v4는 동일한 캐시 이름(`searchCache`)을 사용하면서 `ProductSearchResult`로 꺼내려 했기 때문에 타입 충돌이 발생했습니다.

**해결 방법**  
v4 전용 캐시 이름을 별도로 분리하여 v2와 v4가 각자 독립된 캐시 공간을 사용하도록 변경했습니다.

    public final static String V2_CACHE_NAME = "searchCache";
    public final static String V4_CACHE_NAME = "v4-searchCache";

---

### 4. 캐시 HIT 시 `@Around` AOP 미동작 문제

**문제 상황**  
캐시 HIT/MISS 로그를 남기기 위해 `@Around` AOP를 적용했으나, 캐시 HIT인 경우에는 로그가 전혀 찍히지 않았습니다.

**원인 분석**  
Spring Cache의 `CacheInterceptor`가 `@Around`보다 높은 우선순위로 동작합니다.  
캐시 HIT이 발생하면 `CacheInterceptor`가 메서드 체인을 조기 종료(Short-circuit)하여 이후 체인에 등록된 사용자 `@Around` 어드바이스까지 실행이 도달하지 못했던 것입니다.

**해결 방법**  
`@Order(1)`을 추가하여 `CacheLoggingAspect`가 `CacheInterceptor`보다 먼저(바깥에서) 실행되도록 순서를 지정했습니다.  
이렇게 하면 캐시 HIT 여부와 관계없이 `@Around`가 항상 먼저 실행됩니다.

    @Aspect
    @Component
    @Order(1)
    public class CacheLoggingAspect { ... }
