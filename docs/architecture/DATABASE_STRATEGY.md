# 데이터베이스 전략 및 TimescaleDB 활용

## TimescaleDB와 PostgreSQL의 관계

### 핵심 개념

**TimescaleDB는 PostgreSQL의 확장(Extension)입니다.**

- TimescaleDB ≠ 별도의 데이터베이스
- TimescaleDB = PostgreSQL + TimescaleDB Extension
- 같은 PostgreSQL 인스턴스에서 사용 가능

### 아키텍처 옵션

#### 옵션 1: 단일 PostgreSQL 인스턴스 (MVP 권장)

```
PostgreSQL 인스턴스 (TimescaleDB Extension 포함)
├── trading_db (데이터베이스)
    ├── public 스키마
    │   ├── trades (거래 이력)
    │   ├── positions (포지션)
    │   ├── risk_audit_logs (리스크 감사 로그)
    │   └── trading_config (설정)
    └── timeseries 스키마
        └── candles (캔들스틱 - TimescaleDB 하이퍼테이블)
```

**장점**:
- 단순한 구조
- 하나의 연결만 관리
- MVP에 적합

**단점**:
- 데이터베이스 부하가 한 곳에 집중
- 백업/복구가 복합적

#### 옵션 2: 논리적 분리 (같은 인스턴스, 다른 데이터베이스)

```
PostgreSQL 인스턴스 (TimescaleDB Extension 포함)
├── trading_db (일반 데이터)
│   ├── trades
│   ├── positions
│   └── trading_config
└── timeseries_db (시계열 데이터)
    └── candles (TimescaleDB 하이퍼테이블)
```

**장점**:
- 논리적 분리
- 각 데이터베이스별 독립적 백업 가능
- 스키마 관리 용이

**단점**:
- 두 개의 데이터베이스 연결 관리 필요

#### 옵션 3: 물리적 분리 (별도 인스턴스) - Phase 2 이후

```
PostgreSQL 인스턴스 1 (일반 데이터)
└── trading_db
    ├── trades
    ├── positions
    └── trading_config

PostgreSQL 인스턴스 2 (TimescaleDB Extension)
└── timeseries_db
    └── candles (TimescaleDB 하이퍼테이블)
```

**장점**:
- 완전한 독립성
- 각각 독립적으로 스케일링 가능
- 장애 격리

**단점**:
- 복잡한 운영
- 두 인스턴스 관리 필요
- 트랜잭션 분산 처리 복잡

---

## MVP 데이터베이스 전략

### MVP 단계: 단일 PostgreSQL (TimescaleDB Extension 없음)

**이유**:
- 데이터 양이 적음 (1개 코인, 5분봉)
- 시계열 특화 기능 불필요
- 단순한 구조로 빠른 개발

**구조**:
```
PostgreSQL 인스턴스
└── trading_db
    ├── candles (일반 테이블)
    ├── trades
    ├── positions
    ├── risk_audit_logs
    └── trading_config
```

**예상 데이터 양**:
- 5분봉 × 288개/일 × 365일 = 105,120건/년
- 일반 PostgreSQL로 충분히 처리 가능

---

## Phase 2: TimescaleDB 도입 전략

### 단계별 마이그레이션

#### Step 1: TimescaleDB Extension 설치

```sql
-- PostgreSQL에 TimescaleDB Extension 추가
CREATE EXTENSION IF NOT EXISTS timescaledb;
```

#### Step 2: 기존 테이블을 하이퍼테이블로 변환

```sql
-- 기존 candles 테이블을 하이퍼테이블로 변환
SELECT create_hypertable('candles', 'open_time');
```

또는

#### Step 3: 새로운 하이퍼테이블 생성 (권장)

```sql
-- 새로운 하이퍼테이블 생성
CREATE TABLE candles_ts (
    id BIGSERIAL,
    symbol VARCHAR(10) NOT NULL,
    open_time TIMESTAMPTZ NOT NULL,
    open_price DECIMAL(20, 8) NOT NULL,
    high_price DECIMAL(20, 8) NOT NULL,
    low_price DECIMAL(20, 8) NOT NULL,
    close_price DECIMAL(20, 8) NOT NULL,
    volume DECIMAL(20, 8) NOT NULL,
    PRIMARY KEY (symbol, open_time)
);

-- 하이퍼테이블로 변환
SELECT create_hypertable('candles_ts', 'open_time');

-- 기존 데이터 마이그레이션
INSERT INTO candles_ts SELECT * FROM candles;
```

---

## 데이터 저장 전략 상세

### 1. 원천 데이터 (PostgreSQL 일반 테이블)

**저장 대상**:
- 거래 이력 (`trades`)
- 활성 포지션 (`positions`)
- 리스크 감사 로그 (`risk_audit_logs`)
- 트레이딩 설정 (`trading_config`)

**특징**:
- 관계형 데이터
- 트랜잭션 중요
- CRUD 작업 빈번

**스키마 예시**:
```sql
CREATE TABLE trades (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    order_type VARCHAR(10) NOT NULL, -- BUY, SELL
    price DECIMAL(20, 8) NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    executed_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE positions (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    entry_price DECIMAL(20, 8) NOT NULL,
    quantity DECIMAL(20, 8) NOT NULL,
    entry_time TIMESTAMPTZ NOT NULL,
    stop_loss DECIMAL(20, 8),
    take_profit DECIMAL(20, 8),
    status VARCHAR(20) NOT NULL, -- ACTIVE, CLOSED
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### 2. 시계열 데이터 (TimescaleDB 하이퍼테이블)

**저장 대상**:
- 캔들스틱 데이터 (`candles`)
- 가격 틱 데이터 (향후)
- 기술적 지표 계산 결과 (향후)

**특징**:
- 시간 순서 데이터
- 대량 데이터 (수백만 건)
- 시간 범위 조회 빈번
- 자동 압축 필요

**스키마 예시**:
```sql
-- TimescaleDB Extension 활성화
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- 하이퍼테이블 생성
CREATE TABLE candles (
    symbol VARCHAR(10) NOT NULL,
    open_time TIMESTAMPTZ NOT NULL,
    open_price DECIMAL(20, 8) NOT NULL,
    high_price DECIMAL(20, 8) NOT NULL,
    low_price DECIMAL(20, 8) NOT NULL,
    close_price DECIMAL(20, 8) NOT NULL,
    volume DECIMAL(20, 8) NOT NULL,
    PRIMARY KEY (symbol, open_time)
);

-- 하이퍼테이블로 변환 (파티셔닝 설정)
SELECT create_hypertable(
    'candles',
    'open_time',
    chunk_time_interval => INTERVAL '7 days'
);

-- 인덱스 생성
CREATE INDEX idx_candles_symbol_time ON candles (symbol, open_time DESC);
```

### 3. 캐시 레이어 (Redis) - Phase 2

**저장 대상**:
- 대시보드 집계 데이터
- 실시간 기술적 지표
- 세션 데이터

**특징**:
- 빠른 조회
- 임시 데이터
- TTL 설정

---

## 데이터 흐름

### MVP 단계

```
바이낸스 API
    ↓
데이터 수집 모듈
    ↓
PostgreSQL (candles 테이블)
    ↓
데이터 가공 모듈 (SMA, RSI 계산)
    ↓
AI 모듈 (판단)
    ↓
코인 매매 모듈
    ↓
PostgreSQL (trades, positions 테이블)
```

### Phase 2 (TimescaleDB 도입 후)

```
바이낸스 API
    ↓
데이터 수집 모듈
    ↓
PostgreSQL (TimescaleDB Extension)
    ├── candles (하이퍼테이블) ← 시계열 데이터
    └── trades, positions (일반 테이블) ← 관계형 데이터
    ↓
데이터 가공 모듈
    ↓
Redis (캐시) ← 대시보드용 집계 데이터
    ↓
AI 모듈
    ↓
코인 매매 모듈
```

---

## 마이그레이션 가이드

### MVP → Phase 2 마이그레이션

#### 1. TimescaleDB Extension 설치

```sql
-- PostgreSQL에 TimescaleDB Extension 추가
CREATE EXTENSION IF NOT EXISTS timescaledb;
```

#### 2. 기존 테이블 확인

```sql
-- 기존 candles 테이블 구조 확인
\d candles
```

#### 3. 하이퍼테이블로 변환

**방법 A: 기존 테이블 변환 (데이터 보존)**

```sql
-- 기존 테이블을 하이퍼테이블로 변환
SELECT create_hypertable('candles', 'open_time');
```

**방법 B: 새 테이블 생성 후 마이그레이션 (권장)**

```sql
-- 1. 새 하이퍼테이블 생성
CREATE TABLE candles_ts (
    symbol VARCHAR(10) NOT NULL,
    open_time TIMESTAMPTZ NOT NULL,
    open_price DECIMAL(20, 8) NOT NULL,
    high_price DECIMAL(20, 8) NOT NULL,
    low_price DECIMAL(20, 8) NOT NULL,
    close_price DECIMAL(20, 8) NOT NULL,
    volume DECIMAL(20, 8) NOT NULL,
    PRIMARY KEY (symbol, open_time)
);

SELECT create_hypertable('candles_ts', 'open_time');

-- 2. 데이터 마이그레이션
INSERT INTO candles_ts 
SELECT * FROM candles;

-- 3. 애플리케이션 코드 수정 (테이블명 변경)

-- 4. 기존 테이블 백업 후 삭제
ALTER TABLE candles RENAME TO candles_old;
ALTER TABLE candles_ts RENAME TO candles;
```

#### 4. 압축 정책 설정 (선택)

```sql
-- 30일 이상 된 데이터 자동 압축
SELECT add_compression_policy('candles', INTERVAL '30 days');
```

---

## 성능 비교

### 일반 PostgreSQL vs TimescaleDB

#### 쿼리 성능 (100만 건 데이터 기준)

```sql
-- 최근 1시간 데이터 조회
SELECT * FROM candles
WHERE symbol = 'BTCUSDT'
  AND open_time >= NOW() - INTERVAL '1 hour'
ORDER BY open_time;

-- 일반 PostgreSQL: ~500ms
-- TimescaleDB: ~10ms (50배 빠름)
```

#### 저장 공간

```
일반 PostgreSQL: 1GB
TimescaleDB (압축 후): 100MB (90% 절감)
```

---

## 결론 및 권장사항

### MVP 단계
- ✅ **단일 PostgreSQL 사용** (TimescaleDB Extension 없음)
- ✅ 모든 데이터를 일반 테이블로 저장
- ✅ 단순한 구조로 빠른 개발

### Phase 2 (데이터 증가 시)
- ✅ **TimescaleDB Extension 추가**
- ✅ `candles` 테이블을 하이퍼테이블로 변환
- ✅ 압축 정책 설정

### Phase 3 (대규모 확장 시)
- ✅ **물리적 분리 고려** (별도 인스턴스)
- ✅ 읽기 전용 복제본 추가
- ✅ Redis 캐시 레이어 추가

**핵심**: TimescaleDB는 별도의 데이터베이스가 아니라 PostgreSQL의 확장이므로, 같은 인스턴스에서 사용 가능합니다. MVP에서는 단순하게 시작하고, 필요 시 확장하는 전략을 권장합니다.

