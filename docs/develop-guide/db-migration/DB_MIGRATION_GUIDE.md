# DB 마이그레이션 가이드

이 문서는 trading-core 프로젝트에서 Flyway를 사용한 데이터베이스 마이그레이션 관리 방법을 설명합니다.

## 목차

1. [Flyway 개요](#flyway-개요)
2. [마이그레이션 파일 작성](#마이그레이션-파일-작성)
3. [마이그레이션 파일 네이밍 규칙](#마이그레이션-파일-네이밍-규칙)
4. [마이그레이션 실행](#마이그레이션-실행)
5. [일반적인 작업](#일반적인-작업)
6. [문제 해결](#문제-해결)

## Flyway 개요

Flyway는 데이터베이스 스키마 버전 관리 도구로, 다음과 같은 특징이 있습니다:

- **버전 관리**: 마이그레이션 파일을 순차적으로 실행하여 데이터베이스 스키마를 관리
- **이력 추적**: `flyway_schema_history` 테이블에 모든 마이그레이션 실행 이력을 저장
- **자동 실행**: Spring Boot 애플리케이션 시작 시 자동으로 마이그레이션 실행
- **롤백 지원**: 수동 롤백 스크립트 작성 가능

## 마이그레이션 파일 작성

### 파일 위치

모든 마이그레이션 파일은 다음 경로에 위치합니다:

```
src/main/resources/db/migration/
```

### 파일 생성 예시

새로운 마이그레이션 파일을 생성하려면:

1. `src/main/resources/db/migration/` 디렉토리에 SQL 파일 생성
2. 파일명은 네이밍 규칙을 따라야 함 (아래 참조)
3. SQL 문 작성

**예시: 사용자 테이블 생성**

```sql
-- V2__create_users_table.sql
CREATE TABLE IF NOT EXISTS public.users (
    id BIGSERIAL PRIMARY KEY,
    username CHARACTER VARYING(100) NOT NULL UNIQUE,
    email CHARACTER VARYING(255) NOT NULL UNIQUE,
    password_hash CHARACTER VARYING(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
) TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS users_email_idx ON public.users (email);
```

## 마이그레이션 파일 네이밍 규칙

Flyway는 파일명을 기반으로 마이그레이션을 식별하고 순서를 결정합니다.

### 기본 형식

```
V{version}__{description}.sql
```

### 규칙

1. **버전 번호 (V{version})**:
   - `V`로 시작
   - 숫자로 구성 (예: `V1`, `V2`, `V10`)
   - 순차적으로 증가해야 함
   - 중복 불가

2. **구분자 (`__`)**:
   - 두 개의 언더스코어 (`__`)로 버전과 설명을 구분
   - 하나의 언더스코어(`_`)가 아닌 두 개(`__`)를 사용해야 함

3. **설명 ({description})**:
   - 마이그레이션 내용을 설명하는 텍스트
   - 언더스코어로 단어 구분 가능
   - 예: `create_users_table`, `add_email_index_to_users`

### 올바른 예시

```
V1__create_flyway_schema_history.sql
V2__create_users_table.sql
V3__create_orders_table.sql
V4__add_email_index_to_users.sql
V10__create_products_table.sql
```

### 잘못된 예시

```
❌ V1_create_table.sql          (구분자가 하나의 언더스코어)
❌ 1__create_table.sql           (V 접두사 없음)
❌ V1.1__create_table.sql        (버전에 점 사용 불가)
❌ V1__create_table.sql          (이미 V1이 존재하는 경우)
```

## 마이그레이션 실행

### 자동 실행

Spring Boot 애플리케이션을 시작하면 Flyway가 자동으로:

1. `flyway_schema_history` 테이블 확인/생성
2. 마이그레이션 파일 스캔
3. 아직 실행되지 않은 마이그레이션 실행
4. 실행 결과를 `flyway_schema_history` 테이블에 기록

### 설정 확인

`application-db.properties`에서 Flyway 설정을 확인할 수 있습니다:

```properties
# Flyway 활성화
spring.flyway.enabled=true

# 마이그레이션 파일 위치
spring.flyway.locations=classpath:db/migration

# 마이그레이션 검증
spring.flyway.validate-on-migrate=true

# Schema history 테이블명
spring.flyway.table=flyway_schema_history

# Baseline 설정 (기존 DB에 적용 시)
spring.flyway.baseline-on-migrate=false
```

## 일반적인 작업

### 1. 새 테이블 생성

```sql
-- V2__create_products_table.sql
CREATE TABLE IF NOT EXISTS public.products (
    id BIGSERIAL PRIMARY KEY,
    name CHARACTER VARYING(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
) TABLESPACE pg_default;
```

### 2. 컬럼 추가

```sql
-- V3__add_status_to_products.sql
ALTER TABLE public.products 
ADD COLUMN IF NOT EXISTS status CHARACTER VARYING(50) NOT NULL DEFAULT 'ACTIVE';
```

### 3. 인덱스 생성

```sql
-- V4__add_index_to_products.sql
CREATE INDEX IF NOT EXISTS products_status_idx 
ON public.products (status) TABLESPACE pg_default;
```

### 4. 외래 키 추가

```sql
-- V5__add_foreign_key_to_orders.sql
ALTER TABLE public.orders
ADD CONSTRAINT fk_orders_user_id 
FOREIGN KEY (user_id) REFERENCES public.users(id);
```

### 5. 데이터 마이그레이션

```sql
-- V6__migrate_old_user_data.sql
UPDATE public.users 
SET status = 'ACTIVE' 
WHERE status IS NULL;
```

### 6. 복잡한 마이그레이션 (트랜잭션)

```sql
-- V7__complex_migration.sql
BEGIN;

-- 여러 작업 수행
CREATE TABLE IF NOT EXISTS public.categories (...);
ALTER TABLE public.products ADD COLUMN category_id BIGINT;
ALTER TABLE public.products ADD CONSTRAINT fk_category 
    FOREIGN KEY (category_id) REFERENCES public.categories(id);

COMMIT;
```

## 문제 해결

### 1. 마이그레이션 실패 시

마이그레이션이 실패하면:

1. `flyway_schema_history` 테이블에서 실패한 마이그레이션 확인
2. 데이터베이스 상태 확인
3. 마이그레이션 파일 수정 (주의: 이미 실행된 마이그레이션은 수정하면 안 됨)
4. 수동으로 롤백 스크립트 실행
5. 새로운 마이그레이션 파일로 수정 사항 적용

### 2. 체크섬 불일치 오류

마이그레이션 파일이 이미 실행된 후 수정되면 체크섬 불일치 오류가 발생합니다.

**해결 방법:**
- 이미 실행된 마이그레이션 파일은 절대 수정하지 않음
- 변경이 필요하면 새로운 마이그레이션 파일 생성

### 3. 버전 충돌

같은 버전 번호의 마이그레이션 파일이 여러 개 있으면 오류가 발생합니다.

**해결 방법:**
- 각 마이그레이션 파일은 고유한 버전 번호를 가져야 함
- 버전 번호는 순차적으로 증가해야 함

### 4. Supabase 연결 문제

Supabase를 사용하는 경우:

- 연결 URL 형식 확인: `jdbc:postgresql://${DB_HOST}:${DB_PORT}/postgres`
- 환경 변수 확인: `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`
- 연결 풀링 사용 시 포트 확인 (일반: 5432, 풀링: 6543)

### 5. Prepared Statement 충돌

"prepared statement already exists" 오류가 발생하면:

- JDBC URL에 `?prepareThreshold=0` 파라미터 추가 고려
- 또는 연결 풀 설정 조정

## 베스트 프랙티스

### 1. 항상 `IF NOT EXISTS` 사용

```sql
-- ✅ 좋은 예
CREATE TABLE IF NOT EXISTS public.users (...);
CREATE INDEX IF NOT EXISTS users_email_idx ON public.users (email);

-- ❌ 나쁜 예
CREATE TABLE public.users (...);
```

### 2. 명확한 설명 사용

```sql
-- ✅ 좋은 예
V2__create_users_table.sql
V3__add_email_index_to_users.sql

-- ❌ 나쁜 예
V2__update.sql
V3__fix.sql
```

### 3. 하나의 마이그레이션 = 하나의 논리적 변경

```sql
-- ✅ 좋은 예
-- V2__create_users_table.sql: 사용자 테이블 생성
-- V3__create_orders_table.sql: 주문 테이블 생성

-- ❌ 나쁜 예
-- V2__create_tables.sql: 여러 테이블을 한 번에 생성
```

### 4. 롤백 가능한 마이그레이션 작성

가능한 경우 롤백 스크립트도 함께 작성:

```sql
-- V2__create_users_table.sql (업그레이드)
CREATE TABLE IF NOT EXISTS public.users (...);

-- V2.1__rollback_create_users_table.sql (롤백)
DROP TABLE IF EXISTS public.users;
```

### 5. 테스트 환경에서 먼저 검증

프로덕션에 적용하기 전에 로컬/개발 환경에서 먼저 테스트

## 현재 마이그레이션 상태 확인

### 데이터베이스에서 확인

```sql
SELECT * FROM public.flyway_schema_history 
ORDER BY installed_rank DESC;
```

### 애플리케이션 로그 확인

애플리케이션 시작 시 Flyway가 마이그레이션 실행 로그를 출력합니다:

```
Flyway Community Edition 11.x.x by Redgate
Database: jdbc:postgresql://...
Successfully validated 5 migrations (execution time 00:00.012s)
Current version of schema "public": 5
Migrating schema "public" to version "6" - Add email index
Successfully applied 1 migration to schema "public" (execution time 00:00.045s)
```

## 참고 자료

- [Flyway 공식 문서](https://flywaydb.org/documentation/)
- [Spring Boot Flyway 통합](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
- [PostgreSQL 문서](https://www.postgresql.org/docs/)

