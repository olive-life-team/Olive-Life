## 🚨 상품 검색 쿼리 성능 개선
상품 검색 API에서 카테고리, 상태, 상품명 조건이 함께 들어가는 조회 쿼리를 사용하고 있었는데, 정렬까지 포함되면서 실행 계획상 `Using filesort`가 발생했다.  
검색 기능은 사용자 요청이 자주 들어오는 영역이라, 조회 성능이 느려지면 응답 속도와 사용자 경험에 직접적인 영향을 줄 수 있었다.

---

## 🔎 배경
프로젝트에서 상품 검색 기능을 구현하면서, 단순 조회가 아니라 아래 요소가 함께 들어가는 쿼리를 사용하고 있었다.

- `category_id` 조건
- `status` 조건
- `name` 접두어 검색
- `name` 기준 정렬

처음에는 카테고리 단일 인덱스만 있어도 어느 정도 조회가 가능할 것이라고 생각했다.  
하지만 검색 조건과 정렬 조건이 함께 들어가다 보니, 데이터가 늘어날수록 인덱스 설계를 더 정확하게 해야 한다고 판단했다.

성능 개선 결과를 확인하기 위해 더미 데이터를 적재하고, 실행 계획과 반복 실행 시간을 함께 비교했다.

---

## ⚠️ 문제 상황
기존 상품 테이블에는 `category_id` 단일 인덱스만 적용된 상태였다.  
이 상태에서 실행 계획을 확인해 보니 정렬 단계에서 `Using filesort`가 발생하고 있었고, 조건이 늘어날수록 불필요한 정렬 비용이 생길 수 있는 구조였다.

#### Before EXPLAIN
| id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra |
|:--:|:-----------:|:-----:|:----------:|:----:|:-------------|:---|:-------:|:---:|:----:|:--------:|:------|
| 1 | SIMPLE | product | null | ref | idx_product_category_id | idx_product_category_id | 8 | const | 3571 | 3.7 | Using where; Using filesort |

기존 인덱스는 카테고리 조건까지만 효율적으로 처리하고 있었고,  
`status` 조건과 `name` 검색 + 정렬까지는 충분히 커버하지 못했다.

---

## 문제 재현 및 측정
성능 차이를 더 명확하게 보기 위해 더미 데이터를 적재한 뒤 반복 실행으로 시간을 비교했다.

### 더미데이터 적재 결과
| 항목 | 값 |
| --- | --- |
| 적재 건수 | 50,000건 |
| 전체 상품 수 | 50,000건 |
| 적재 시간 | 3,035ms |

### 실행 시간 비교
| 구분 | min(ms) | max(ms) | avg(ms) |
| --- | ---: | ---: | ---: |
| before | 4 | 5 | 4.6 |
| after | 1 | 2 | 1.7 |

동일한 조회 쿼리를 여러 번 반복 실행한 뒤 최소, 최대, 평균 시간을 비교했고, 평균 기준으로 성능 차이를 확인했다.

---

## ⚠️ 원인 분석
문제의 핵심은 **검색 조건과 정렬 조건을 단일 인덱스가 충분히 커버하지 못했다는 점**이었다.

기존 인덱스는 `category_id`만 고려하고 있었기 때문에, 실제 조회 쿼리에서 함께 사용되는

- `status`
- `name`

조건을 효율적으로 처리하지 못했다.

특히 `name`은 접두어 검색과 정렬에 동시에 사용되고 있었는데, 이 컬럼이 인덱스 설계에 포함되지 않으면 DB는 추가 정렬 작업을 수행해야 한다.

즉, 이 문제는 단순히 “인덱스가 있느냐 없느냐”가 아니라,  
**실제 조회 조건 순서에 맞는 복합 인덱스를 설계했느냐**가 핵심이었다.

---

## 💡 해결 방식
조회 패턴에 맞춰 복합 인덱스를 다시 설계했다.

### 적용한 인덱스
```sql
create index idx_product_category_status_name
    on product (category_id, status, name);
```

이 인덱스는 다음 기준으로 설계했다.

1. `category_id`로 먼저 범위를 좁히고
2. `status`로 한 번 더 필터링하고
3. `name`으로 접두어 검색과 정렬까지 함께 처리하도록 구성

즉, 실제 쿼리의 조건 순서와 사용 목적을 기준으로 인덱스를 구성한 것이다.

#### After EXPLAIN
| id | select_type | table | partitions | type | possible_keys | key | key_len | ref | rows | filtered | Extra |
|:--:|:-----------:|:-----:|:----------:|:----:|:-------------|:---|:-------:|:---:|:----:|:--------:|:------|
| 1 | SIMPLE | product | null | range | idx_product_category_id, idx_product_category_status_name | idx_product_category_status_name | 411 | null | 350 | 100 | Using where; Using index |

After 실행 계획에서는 다음 변화가 있었다.

- 사용 인덱스가 `idx_product_category_id`에서 `idx_product_category_status_name`으로 변경됨
- 조회 방식이 `ref`에서 `range`로 바뀜
- 예상 조회 rows가 `3571` → `350`으로 감소
- Before에서 보이던 `Using filesort`가 사라짐

즉, 복합 인덱스가 검색 조건과 정렬 조건을 함께 처리하면서 조회 대상 범위를 더 좁힐 수 있었다.

---

## 해결 포인트 정리
- 단일 컬럼 인덱스만으로는 검색 + 정렬이 함께 들어가는 쿼리를 충분히 최적화하기 어려웠다.
- 복합 인덱스는 실제 조회 패턴의 조건 순서에 맞춰 설계해야 효과가 크다.
- 성능 개선은 감으로 판단하지 않고 `EXPLAIN`과 반복 실행 시간 비교로 검증해야 한다.

---

## ✅ 결과
복합 인덱스 적용 후 반복 실행 기준 조회 성능이 개선되었다.

- before 평균: `4.6ms`
- after 평균: `1.7ms`

평균 기준으로 약 **63.04%** 개선되었다.

```text
(4.6 - 1.7) / 4.6 × 100 ≈ 63.04%
```

즉, 단순히 인덱스를 추가한 것이 아니라,  
**조회 조건과 정렬 기준에 맞는 인덱스를 설계했을 때 실제 응답 시간이 유의미하게 줄어든 것**을 확인할 수 있었다.

---

## 📚 학습한 점
이번 경험을 통해 인덱스는 “많이 걸수록 좋다”가 아니라,  
**실제 쿼리 패턴에 맞게 설계해야 의미가 있다**는 점을 배웠다.

또한 성능 개선에서는 다음 흐름이 중요하다고 느꼈다.

- 실행 계획 확인
- 테스트 데이터 확보
- before / after 반복 측정
- 개선 근거를 숫자로 검증

즉, 성능 최적화는 추상적인 튜닝이 아니라  
**가설을 세우고, 실행 계획과 수치로 검증하는 과정**이라는 기준을 갖게 되었다.

---

## ✍️ 한 줄 정리
검색 조건과 정렬 조건이 함께 들어가는 쿼리는 단일 인덱스보다 조회 패턴에 맞춘 복합 인덱스 설계가 훨씬 중요하다는 점을 실제 측정으로 확인했다.

---

## 🚨 GitHub Actions와 AWS Parameter Store를 분리해 CI/CD와 운영 설정을 관리한 경험
GitHub Actions 기반 CI/CD를 구성하면서, 처음에는 테스트가 통과하면 이후 배포도 자연스럽게 이어질 수 있다고 생각했다.  
하지만 실제로는 테스트 환경과 운영 환경의 목적이 다르고, 외부 인프라 의존성과 민감정보 관리 방식도 다르기 때문에 **CI와 CD를 같은 기준으로 설계하면 파이프라인이 불안정해질 수 있다**는 점을 확인했다.

특히 테스트 실행 과정에서 Redis 관련 연결 오류를 겪었고, 동시에 배포 파이프라인을 구성하면서 GitHub Secrets와 AWS Parameter Store에 어떤 값을 나눠서 저장해야 할지도 같이 고민하게 되었다.

---

## 🔎 배경
프로젝트 요구사항에는 다음과 같은 CI/CD 흐름이 포함되어 있었다.

- `main` 또는 `develop` 브랜치에 Push / PR 이벤트 발생 시 자동 워크플로우 실행
- 소스 코드 체크아웃
- JDK 설정 및 Gradle/Maven 빌드
- 테스트 실행
- Docker 이미지 빌드 및 AWS ECR Push
- PR 단계에서는 테스트만, `main` 머지 시에만 Docker Push 하도록 분기 고려

처음에는 CI/CD를 하나의 자동화 흐름으로 단순하게 생각했다.  
하지만 실제로 구성해보니 아래 질문이 계속 생겼다.

- CI에서 테스트는 H2로 돌릴지, MySQL을 직접 띄울지
- Redis가 필요한 기능이 있을 때 테스트 환경에서도 Redis 연결이 꼭 필요한지
- GitHub Actions에는 어떤 값까지 저장해야 하는지
- DB / JWT / Redis 같은 운영 설정은 어디서 관리해야 하는지
- PR 단계와 main 배포 단계를 왜 분리해야 하는지

즉, 단순히 워크플로우를 만드는 것보다  
**테스트 환경, 배포 자격 증명, 운영 런타임 설정의 책임을 어떻게 나눌지 설계하는 과정**이 더 중요했다.

---

## ⚠️ 문제 상황
실제로 테스트 실행 과정에서 Redis 관련 Bean 초기화 때문에 애플리케이션 컨텍스트 로딩에 실패하는 문제가 있었다.

문제 흐름은 아래와 같았다.

- `./gradlew test` 실행
- Spring Boot 테스트 컨텍스트 로딩
- Redis 관련 설정 또는 Listener 초기화
- 테스트 환경에서 Redis 호스트를 찾지 못하거나 연결 실패
- `contextLoads()` 단계부터 테스트 실패

즉, 비즈니스 로직이 잘못된 것이 아니라  
**테스트 환경이 외부 인프라 구성에 과하게 의존하고 있었던 것**이 문제였다.

### 실제로 확인했던 에러 로그
```text
Run ./gradlew test
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava
> Task :processTestResources
> Task :testClasses

> Task :test

ChatDemoApplicationTests > contextLoads() FAILED
    java.lang.IllegalStateException at DefaultCacheAwareContextLoaderDelegate.java:180
        Caused by: org.springframework.context.ApplicationContextException at DefaultLifecycleProcessor.java:288
            Caused by: org.springframework.data.redis.listener.adapter.RedisListenerExecutionFailedException at RedisMessageListenerContainer.java:383
                Caused by: org.springframework.data.redis.RedisConnectionFailureException at LettuceConnectionFactory.java:1849
                    Caused by: org.springframework.data.redis.connection.PoolException at LettucePoolingConnectionProvider.java:104
                        Caused by: io.lettuce.core.RedisConnectionException at RedisConnectionException.java:78
                            Caused by: java.net.UnknownHostException at Inet6AddressImpl.java:-2
```

---

## ⚠️ 원인 분석
이 문제를 단순히 “Redis 연결 에러”로 보지 않고, CI/CD 설계 관점에서 다시 정리했다.

### 1. 테스트 환경과 운영 환경의 목적이 달랐다
운영 환경에서는 MySQL, Redis, 환경변수, 배포 대상 서버가 모두 실제로 맞물려 동작해야 한다.  
반면 CI의 test 단계는 기본적으로 **코드 변경이 애플리케이션 품질을 깨지 않았는지 빠르게 검증하는 단계**다.

즉, 모든 테스트가 운영 인프라와 동일한 조건을 요구할 필요는 없었다.

### 2. 외부 인프라 의존성이 CI 안정성을 떨어뜨릴 수 있었다
Redis처럼 외부 연결이 필요한 구성이 테스트 컨텍스트에 그대로 포함되면,  
테스트 코드가 문제가 없어도 아래 이유로 실패할 수 있다.

- Redis 호스트를 찾지 못함
- 테스트 환경에 Redis가 떠 있지 않음
- Docker Compose 기준 서비스명이 CI 환경에서는 그대로 동작하지 않음

이 경우 테스트 실패 원인이 코드인지, 환경인지 구분이 어려워진다.

### 3. 배포 자격 증명과 운영 런타임 설정의 성격이 달랐다
CI/CD를 구성하면서 GitHub Actions에는 AWS 접근과 배포에 필요한 최소 정보만 두고,  
실제 애플리케이션 런타임에 필요한 민감정보는 별도 저장소에서 관리하는 것이 더 적절하다고 판단했다.

실제로 GitHub Secrets에는 다음과 같은 값들을 관리했다.

- `APP_PORT`
- `AWS_ACCESS_KEY_ID`
- `AWS_REGION`
- `AWS_SECRET_ACCESS_KEY`
- `EC2_INSTANCE_ID`
- `ECR_REGISTRY`
- `ECR_REPOSITORY`
- `ECR_URI`

반면 운영 설정은 AWS Systems Manager Parameter Store에 아래와 같이 분리했다.

- `/olive-life/prod/DB_PASSWORD`
- `/olive-life/prod/DB_URL`
- `/olive-life/prod/DB_USERNAME`
- `/olive-life/prod/JWT_ACCESS_TOKEN_EXPIRATION_MS`
- `/olive-life/prod/JWT_REFRESH_TOKEN_EXPIRATION_MS`
- `/olive-life/prod/JWT_SECRET`
- `/olive-life/prod/REDIS_HOST`
- `/olive-life/prod/REDIS_PORT`

즉,

- **GitHub Secrets**: CI/CD 파이프라인이 AWS에 접근하고 배포를 수행하기 위한 정보
- **Parameter Store**: 운영 애플리케이션이 실제 실행 중 필요로 하는 DB / JWT / Redis 설정

으로 역할을 나누는 것이 더 명확했다.

### 원인과 관련된 실제 설정 흐름
```yaml
spring:
  application:
    name: teampage

  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
```

이처럼 설정이 환경변수 기반으로 구성되어 있으면 유연하다는 장점은 있지만,  
반대로 말하면 **환경별로 어떤 값을 어디서 주입할지 분리하지 않으면 CI와 운영이 서로 영향을 줄 수 있다**는 뜻이기도 했다.

---

## 💡 해결 방식
이 경험을 통해 CI/CD를 설계할 때 기준을 다음처럼 정리했다.

### 1. PR 단계와 배포 단계를 분리해서 생각했다
요구사항에서도 **PR 단계에서는 테스트만**, **main 머지 시 Docker Push**를 고려하도록 되어 있었다.  
이 기준을 받아들여, PR 단계의 목적은 빠른 품질 검증이고, 실제 이미지 생성 및 배포 준비는 main 단계에서 다루는 것이 더 맞다고 판단했다.

즉,

- PR: 테스트 중심
- main: 빌드 / 이미지 / 배포 중심

으로 역할을 분리하는 방향이 합리적이라고 정리했다.

### 2. 테스트 환경은 가능한 한 외부 인프라 의존성을 줄이는 방향으로 고민했다
실제로 H2를 테스트용으로 써야 하는지, MySQL을 CI에서 직접 띄워야 하는지 계속 고민했다.  
이 과정에서 정리한 기준은 다음과 같았다.

- 일반적인 테스트는 가볍고 빠르게 검증할 수 있는 환경이 유리하다
- MySQL 의존 SQL이나 실제 DB 동작 검증이 꼭 필요한 경우는 별도로 본다
- Redis가 꼭 필요하지 않은 테스트는 Redis 의존성을 최대한 분리한다

즉, 모든 테스트를 운영과 동일하게 맞추기보다  
**테스트 목적에 따라 검증 범위를 나누는 것이 더 현실적**이라고 판단했다.

### 3. GitHub Secrets와 Parameter Store의 역할을 나눴다
민감정보를 한곳에 몰아넣기보다, 성격에 따라 저장소를 분리하는 쪽이 더 관리하기 쉬웠다.

- GitHub Secrets: 배포 파이프라인용
- Parameter Store: 운영 애플리케이션 설정용

이렇게 나누면 배포 도구가 꼭 알아야 하는 값과, 운영 애플리케이션이 실행 시 필요한 값을 구분할 수 있었다.

### 4. 환경변수와 프로필 분리를 더 중요하게 보게 되었다
프로젝트를 진행하면서 실제로 아래 요소들을 계속 나눠서 다뤘다.

- `local`
- `docker`
- `prod`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`
- `.env`, `.env.example`
- `application.yml`, `application-prod.yml`

이 경험을 통해, CI/CD 설계에서는 단순히 YAML을 작성하는 것보다  
**어떤 환경에서 어떤 값이 주입되어야 하는지 명확히 구분하는 것**이 더 중요하다고 느꼈다.

---

## 해결 포인트 정리
- PR 단계와 배포 단계를 같은 기준으로 보지 않고 역할을 분리했다.
- 테스트 환경은 빠르고 안정적으로 유지하기 위해 외부 인프라 의존성을 줄이는 방향으로 고민했다.
- GitHub Secrets와 AWS Parameter Store의 역할을 나눠 민감정보와 런타임 설정을 분리했다.
- 환경변수와 프로필을 명확히 구분해야 CI/CD가 흔들리지 않는다는 기준을 세웠다.

---

## ✅ 결과
이 경험을 통해 단순히 “GitHub Actions를 붙였다” 수준이 아니라,  
**어떤 기준으로 CI와 CD를 나누고, 테스트 환경과 운영 설정 저장소를 어떻게 분리할지**를 더 명확히 이해하게 되었다.

특히 아래와 같은 관점이 정리되었다.

- PR 단계에서는 빠른 테스트 검증이 중요하다.
- 운영 인프라 의존성이 강한 테스트는 CI 안정성을 해칠 수 있다.
- 배포 자격 증명과 운영 애플리케이션 설정은 같은 저장소에 두지 않는 것이 관리상 더 명확하다.
- CI/CD는 자동화 도구를 붙이는 것보다, 검증 기준과 설정 관리 기준을 설계하는 과정이 더 중요하다.

---

## 📚 학습한 점
이번 경험을 통해 가장 크게 배운 점은,  
**CI/CD는 단순히 테스트와 배포를 자동화하는 작업이 아니라, 각 단계의 목적과 설정의 책임을 분리해서 설계하는 작업**이라는 점이었다.

처음에는 “테스트 통과 → 배포”라는 흐름만 생각했지만, 실제로는 그 사이에 다음과 같은 설계 판단이 필요했다.

- PR 단계에서 어디까지 검증할 것인가
- 테스트에서 외부 인프라를 어디까지 포함할 것인가
- 배포 파이프라인이 알아야 하는 값과, 운영 애플리케이션이 알아야 하는 값을 어떻게 분리할 것인가
- 프로필과 환경변수를 어떻게 나눌 것인가

즉, 이 경험은 단순한 Redis 에러 해결이 아니라,  
**CI/CD를 설계할 때 어떤 기준을 먼저 세워야 하는지 배우게 된 계기**였다.

---

## ✍️ 한 줄 정리
CI/CD를 구성하면서, 테스트 환경과 운영 환경의 목적이 다르기 때문에 외부 인프라 의존성, 배포 자격 증명, 운영 설정 저장소를 분리해 설계해야 한다는 점을 배웠다.