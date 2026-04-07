# Commerce API — Query Model Separation

Spring Boot 기반 커머스 백엔드 API 프로젝트입니다.
레포지토리 이름 그대로 **"계층 간 모델 분리(Query/Command Model Separation)"** 를 설계의 중심 축으로 잡고, 외부 API 계약 · 서비스 도메인 · 영속성 엔티티를 명확히 분리한 구조를 보여주는 것이 목적입니다.

> **📁 레포 구조 안내**
> 이 레포지토리는 root 에 프로젝트 메타 정보(README, `.env.example`, `.gitignore`)를, `commerce/` 하위 디렉터리에 실제 Spring Boot 애플리케이션 소스를 둡니다. 이후 CDC 이벤트 파이프라인, Elasticsearch/MongoDB query model 등 추가 컴포넌트가 같은 레포에 공존하도록 **모노레포 스타일**로 확장하기 위한 구조입니다.
> 실제 프로젝트 루트는 `commerce/` 입니다. 빌드/실행은 `cd commerce && ./gradlew bootRun`.

---

## 1. 브랜치 구성 — 아키텍처 진화 스토리

이 레포는 단일 `main` 브랜치가 아니라, **아키텍처가 단계적으로 진화하는 과정을 브랜치로 표현**합니다. 각 브랜치를 순서대로 따라가면 CQRS + CDC 기반 읽기 모델 분리가 어떻게 구현되는지 볼 수 있습니다.

| Branch | 주제 | 설명 |
|---|---|---|
| `main` | Baseline 모놀리식 | 단일 PostgreSQL, JPA 기반 CRUD + 동적 검색. 아래 2~4장의 아키텍처가 여기에 해당. |
| `1-command-model-separation` | Command / Query 서비스 분리 | Read/Write 서비스 레이어를 분리하고 docker-compose 로 DB 구성. |
| `2-cdc-event` | CDC 이벤트 파이프라인 | Kafka + Debezium 으로 PostgreSQL 변경 이벤트를 스트리밍. |
| `3-query-model-separation` | Query Model 실제 분리 | MongoDB + Elasticsearch(nori) + Redis 를 Query 전용 저장소로 사용. CDC 로 동기화. |

리뷰 시에는 `main` → `1-` → `2-` → `3-` 순서로 보시면 점진적 리팩토링 의도를 파악하기 쉽습니다.

---

## 2. 설계 목표 (Why this repo exists)

실무에서 JPA 기반 Spring 프로젝트가 흔히 겪는 문제를 의도적으로 방어하는 구조를 만들었습니다.

| 자주 발생하는 문제 | 이 프로젝트에서의 대응 |
|---|---|
| Entity를 그대로 응답으로 내보내 LAZY 프록시 / 순환참조 / 스키마 유출 발생 | **Entity는 절대 Controller 경계를 넘지 않음** |
| Controller가 Service 내부 모델에 직접 의존해 API 스펙과 도메인 로직이 함께 흔들림 | **Controller DTO ↔ Service DTO** 를 분리하고 Mapper로만 변환 |
| 조회(Query)와 변경(Command)이 같은 모델을 공유해 읽기 화면 요구사항이 도메인을 오염 | **읽기 전용 서비스(MainService)** 를 분리해 화면 조합 전용 모델 사용 (이후 브랜치에서 저장소 수준까지 확장) |
| 검색/필터 API가 `if` 떡칠로 비대해짐 | **JPA Specification** 으로 동적 조건을 조합 가능한 단위로 분해 |
| 응답 포맷이 엔드포인트마다 제각각 | **공통 `ApiResponse<T>`** 래퍼로 통일, snake_case 직렬화 일관화 |
| DB 자격증명이 소스에 섞여 유출 위험 | **Spring Boot Relaxed Binding** 으로 `SPRING_DATASOURCE_*` 환경변수 주입. yaml 은 `REDACTED` 플레이스홀더만 유지, `.env.example` 로 가이드 |

---

## 3. 아키텍처 개요 (main 브랜치 기준)

### 3.1 레이어 구성

```
 ┌────────────────────────────────────────────────────────┐
 │                    HTTP Client                          │
 └────────────────────────────────────────────────────────┘
                         │  JSON (snake_case)
                         ▼
 ┌────────────────────────────────────────────────────────┐
 │  Controller Layer         (wanted.commerce.controller)  │
 │  - ProductController / ReviewController / ...          │
 │  - Controller DTO  (*Request / *Response)              │
 │  - ControllerMapper : Controller DTO ⇄ Service DTO     │
 │  - ApiResponse<T> / ErrorResponse  (공통 응답 포맷)     │
 └────────────────────────────────────────────────────────┘
                         │  Service DTO
                         ▼
 ┌────────────────────────────────────────────────────────┐
 │  Service Layer            (wanted.commerce.service)     │
 │  - ProductService  (Command: 생성/수정/삭제 + 목록)     │
 │  - MainService     (Query : 메인페이지 조합 전용)       │
 │  - ReviewService / CategoryService                     │
 │  - Service DTO (도메인 입출력 모델)                      │
 │  - ServiceMapper : Service DTO ⇄ Entity                │
 └────────────────────────────────────────────────────────┘
                         │  Entity
                         ▼
 ┌────────────────────────────────────────────────────────┐
 │  Repository Layer         (wanted.commerce.repository)  │
 │  - Spring Data JPA Repository                          │
 │  - ProductSpecification (동적 검색 조건)                │
 └────────────────────────────────────────────────────────┘
                         │
                         ▼
 ┌────────────────────────────────────────────────────────┐
 │  PostgreSQL  (schema: wanted)                           │
 └────────────────────────────────────────────────────────┘
```

### 3.2 핵심 원칙: 3-Layer Model Separation

한 개의 "상품 데이터"가 요청에서 응답까지 지나가는 동안, **3종류의 서로 다른 모델**을 거치며 각 계층이 독립적으로 진화할 수 있게 했습니다.

```
 ProductCreateRequest        →   ProductDto.CreateRequest    →   Product (Entity)
 (Controller DTO)                (Service DTO)                    (JPA Entity)
     ▲                               ▲                                 ▲
     │ API 계약 (외부 노출)           │ 도메인 유스케이스 입출력         │ DB 스키마 / 영속성
     │ snake_case, validation 등     │ 서비스 조합 로직의 파라미터       │ 연관관계, 컬럼
     ▼                               ▼                                 ▼
 ProductControllerMapper  ──►   ProductMapper          ──►   (JPA)
```

**얻는 것:**
- API 스펙이 바뀌어도 Entity / Service 가 영향받지 않음 (반대도 마찬가지)
- 테스트할 때 각 계층을 독립적으로 모킹/검증 가능
- 응답 필드 숨김, naming convention 변경이 매퍼 1곳에서 끝남

### 3.3 CQRS-lite: Read/Write 서비스 분리

- **`ProductService` (Command side)**
  상품 생성/수정/삭제/옵션/이미지 관리 + 조건 검색 목록.
  `@Transactional` 쓰기 트랜잭션, 무결성 검증 책임.

- **`MainService` (Query side)**
  메인 페이지에 필요한 *신상품 / 인기상품 / 주요 카테고리* 를 화면 관점으로 조합.
  `@Transactional(readOnly = true)`, 오직 `MainPageDto.MainPage` 반환을 위해 존재.
  → 메인 화면 요구사항이 바뀌어도 `ProductService` 의 도메인 로직은 건드리지 않음.

> 이 "서비스 수준 분리" 를 **저장소 수준 분리** 까지 확장한 것이 `3-query-model-separation` 브랜치입니다. MongoDB/Elasticsearch/Redis 를 Query 전용 저장소로 붙이고, CDC(Kafka + Debezium) 로 동기화합니다.

### 3.4 동적 검색: JPA Specification

상품 목록 API는 상태 / 가격 범위 / 카테고리 / 판매자 / 브랜드 / 태그 / 재고 여부 / 검색어 / 등록일 범위 등 다중 조건을 지원합니다.
`ProductSpecification` 에 **조건 하나당 static factory 하나** 로 분해하고,
`ProductServiceImpl.getProducts()` 에서 `Specification.where(null).and(...)` 로 조립합니다.

- 조건이 늘어도 if-else 폭발 없이 한 줄 추가로 끝
- 각 조건이 독립 단위라 테스트/재사용 용이
- 재고 필터는 서브쿼리(`query.subquery`)까지 Specification 안에 캡슐화

### 3.5 공통 응답 포맷

모든 REST 응답은 `ApiResponse<T>` 로 래핑됩니다.
```json
{ "success": true, "message": "...", "data": { ... } }
```
- 클라이언트 파싱 로직 단순화
- 에러는 `ErrorResponse` 로 별도 표준화
- Jackson + Swagger 설정을 통해 API 전체가 **snake_case** 로 직렬화 (`JacksonConfig`, `SwaggerSnakeCaseConfig`)

---

## 4. 도메인 모델

상품 중심의 커머스 도메인입니다.

```
User ─┐
      │
Seller ─── Product ─┬─ ProductDetail     (1:1)
                    ├─ ProductPrice      (1:1)
                    ├─ ProductImage*     (1:N, Option 과 연결 가능)
                    ├─ ProductOptionGroup* ── ProductOption*
                    ├─ Category*         (N:M)
                    ├─ Tag*              (N:M)
                    ├─ Brand             (N:1)
                    └─ Review*           (1:N, User 작성)
```

- `ProductStatus` enum 으로 상품 라이프사이클 관리 (ACTIVE / OUT_OF_STOCK / DELETED ...)
- **소프트 삭제**: `deleteProduct` 는 `status = DELETED` 로 마킹만 수행
- Category 는 self-reference 계층 구조 (`level`, `slug`)

---

## 5. 기술 스택

| 영역 | 선택 |
|---|---|
| Language / Runtime | Java 17 |
| Framework | Spring Boot 3.4.5, Spring Data JPA, Spring Web |
| Persistence | PostgreSQL (schema `wanted`), Hibernate |
| API Doc | springdoc-openapi 2.8.4 (Swagger UI, snake_case) |
| Boilerplate 감소 | Lombok |
| Build | Gradle (wrapper 포함) |
| Test | JUnit 5, spring-boot-starter-test |

`1-` ~ `3-` 브랜치에서 추가되는 것: Docker Compose, Kafka, Debezium, MongoDB, Elasticsearch(nori), Redis/Valkey.

---

## 6. 프로젝트 구조

```
repo root
├── README.md                # 이 문서
├── .env.example             # 환경변수 템플릿 (복사해서 .env 로 사용)
├── .gitignore               # .env*, 툴 상태 등 차단
└── commerce/                # ← 실제 Spring Boot 프로젝트 루트
    ├── build.gradle
    ├── settings.gradle
    ├── gradlew(.bat)
    ├── gradle/
    └── src/main/java/wanted/commerce
        ├── CommerceApplication.java
        ├── config/                  # Jackson / Swagger snake_case 설정
        ├── controller/              # REST 엔드포인트 + 공통 응답
        │   ├── dto/                 # ── Controller DTO (API 계약)
        │   └── mapper/              # ── Controller DTO ⇄ Service DTO
        ├── service/                 # 유스케이스 구현
        │   ├── dto/                 # ── Service DTO (도메인 입출력)
        │   └── mapper/              # ── Service DTO ⇄ Entity
        ├── repository/              # Spring Data JPA + Specification
        ├── entity/                  # JPA Entity (영속성 모델)
        └── exception/               # 도메인 예외
```

`commerce/` 내부 트리 자체가 **"각 레이어마다 자기만의 DTO와 Mapper를 가진다"** 는 설계 원칙을 그대로 반영합니다.

---

## 7. 실행 방법

### 7.1 사전 준비
- Java 17+
- PostgreSQL (로컬 5432)
- `wanted` 스키마 및 커넥션 정보 설정

### 7.2 환경변수 설정 (자격증명 외부화)

레포에 커밋된 `application.yml` 의 자격증명 필드는 모두 **`REDACTED` 플레이스홀더** 로 치환되어 있습니다. 실제 값은 레포에 들어가지 않습니다.

실행할 때는 **Spring Boot 의 [Relaxed Binding](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.relaxed-binding)** 을 이용해 환경변수로 yaml 값을 덮어씁니다. `application.yml` 은 일절 수정하지 않고, 환경변수만 주입하면 Spring 이 자동으로 매칭시킵니다.

| 환경변수 | 매핑되는 yaml 속성 |
|---|---|
| `SPRING_DATASOURCE_URL`      | `spring.datasource.url` |
| `SPRING_DATASOURCE_USERNAME` | `spring.datasource.username` |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` (optional) | `spring.jpa.hibernate.ddl-auto` |

repo 루트의 `.env.example` 을 복사해서 본인 환경 값으로 채우세요.

```bash
cp .env.example .env
# .env 편집 후 저장
```

`.env` 파일은 `.gitignore` 로 관리되어 **커밋되지 않습니다.** 운영 환경에서는 OS 환경변수, Docker `--env-file`, Kubernetes Secret, AWS Secrets Manager 등으로 동일한 키를 주입하면 됩니다.

> **설계 포인트:** `application.yml` 에는 자격증명을 표현하지 않고, **외부에서 주입 가능한 인터페이스(= Spring 속성 키)** 만 정의합니다. 저장소를 그대로 클론해도 자격증명이 따라오지 않고, 각 환경(로컬/스테이지/프로덕션)은 동일한 코드 베이스에 서로 다른 환경변수 세트만 얹으면 됩니다.

### 7.3 빌드 & 실행

```bash
# 1. .env 를 현재 쉘에 export (bash/zsh)
set -a; source .env; set +a

# 2. commerce/ 로 이동해 Gradle 실행
cd commerce
./gradlew build
./gradlew bootRun
```

Spring Boot 가 `SPRING_DATASOURCE_*` 환경변수를 감지해 `application.yml` 의 `REDACTED` 플레이스홀더를 자동으로 override 합니다. `application.yml` 을 수정하거나 프로파일을 분기시키지 않아도 동작합니다.

기본 포트 `8080` 에서 기동되며, Swagger UI 는:

```
http://localhost:8080/swagger-ui.html
```

---

## 8. 주요 엔드포인트 (발췌)

| Method | Path | 설명 |
|---|---|---|
| POST   | `/api/products`                      | 상품 생성 |
| GET    | `/api/products/{id}`                 | 상품 상세 |
| POST   | `/api/products/{id}`                 | 상품 수정 |
| DELETE | `/api/products/{id}`                 | 상품 삭제(soft) |
| GET    | `/api/products`                      | 상품 목록 (동적 필터 + 페이지네이션) |
| POST   | `/api/products/{id}/options`         | 상품 옵션 추가 |
| POST   | `/api/products/{id}/images`          | 상품 이미지 추가 |
| GET    | `/api/categories`                    | 카테고리 조회 |
| POST   | `/api/reviews`                       | 리뷰 작성 |
| GET    | `/api/main`                          | 메인 페이지 조합(Query 전용) |

모든 응답은 `ApiResponse<T>` 로 감싸져 내려갑니다.

---

## 9. 이 레포지토리를 리뷰하실 때 봐주시면 좋은 포인트

1. **`controller/dto` vs `service/dto` vs `entity`**
   → 같은 "상품"이 3개 층에서 각기 다른 모델로 존재하고, 그 사이를 `*Mapper` 가 책임지는지 확인해주세요.
2. **`ProductServiceImpl` vs `MainServiceImpl`**
   → Command(쓰기/변경) 와 Query(읽기 전용 화면 조합) 가 서비스 단위에서부터 분리되어 있습니다.
3. **`ProductSpecification` + `ProductServiceImpl.getProducts()`**
   → 다중 조건 검색이 if-else 가 아닌 조립형 Specification 으로 구현되어 있습니다.
4. **`ApiResponse` / `JacksonConfig` / `SwaggerSnakeCaseConfig`**
   → API 일관성(포맷 · 네이밍)이 전역 설정으로 묶여 있습니다.
5. **브랜치 진화 흐름**
   → `main` → `1-command-model-separation` → `2-cdc-event` → `3-query-model-separation` 순서로 "단일 DB 모놀리스 → CQRS + CDC 기반 읽기 모델 분리" 로 진화하는 커밋 흐름을 봐주시면 감사합니다.

---

## 10. 앞으로 개선하고 싶은 것

이 프로젝트는 "설계 의도를 명확히 드러내는" 것을 1차 목표로 한 스냅샷입니다. 다음은 실무화할 때 이어서 붙이고 싶은 항목들입니다.

- 인증/인가 (Spring Security + JWT)
- 조회 성능을 위한 QueryDSL 프로젝션 도입 (N+1 방어 + DTO 직접 조회)
- Testcontainers 기반 통합 테스트
- Flyway / Liquibase 로 스키마 버전 관리
- 환경별 프로파일 분리 (`application-{dev,stage,prod}.yml`) 및 Secret Manager 연동
