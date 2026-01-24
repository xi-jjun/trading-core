# TimescaleDB 가이드

## TimescaleDB란?

**TimescaleDB**는 PostgreSQL을 기반으로 한 **시계열 데이터베이스(Time-Series Database)**입니다.

### 핵심 개념

시계열 데이터란 **시간 순서대로 기록되는 데이터**를 의미합니다:
- 암호화폐 가격 데이터 (1분마다 기록)
- 거래량 데이터
- 센서 데이터
- 로그 데이터
- 주식 가격 데이터

---

## 왜 TimescaleDB가 필요한가?

### 일반 PostgreSQL의 한계

```sql
-- 일반 PostgreSQL로 캔들스틱 데이터 저장
CREATE TABLE candles (
    id SERIAL PRIMARY KEY,
    symbol VARCHAR(10),
    open_time TIMESTAMP,
    open_price DECIMAL,
    high_price DECIMAL,
    low_price DECIMAL,
    close_price DECIMAL,
    volume DECIMAL
);

-- 문제점:
-- 1. 데이터가 많아질수록 조회 속도 저하
-- 2. 시간 범위 쿼리가 느림
-- 3. 압축 기능 없어서 저장 공간 낭비
-- 4. 자동 파티셔닝 없어서 관리 복잡
```

### TimescaleDB의 장점

1. **자동 파티셔닝**: 시간 기반으로 데이터를 자동 분할
2. **압축**: 오래된 데이터 자동 압축 (최대 90% 공간 절감)
3. **빠른 시계열 쿼리**: 시간 범위 조회 최적화
4. **PostgreSQL 호환**: 기존 PostgreSQL 도구 그대로 사용 가능
5. **연속 집계**: 실시간 집계 뷰 자동 유지

---

## 트레이딩 시스템에서의 활용

### 데이터 저장 전략

```
원천 데이터 (PostgreSQL)
  ↓
시계열 데이터 (TimescaleDB) ← 캔들스틱, 가격 틱
  ↓
캐시 (Redis) ← 대시보드용 집계 데이터
```

### 예시: 캔들스틱 데이터 저장

#### 1. TimescaleDB 하이퍼테이블 생성

```sql
-- PostgreSQL 확장 활성화
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- 일반 테이블 생성
CREATE TABLE candles (
    time TIMESTAMPTZ NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    open_price DECIMAL(20, 8),
    high_price DECIMAL(20, 8),
    low_price DECIMAL(20, 8),
    close_price DECIMAL(20, 8),
    volume DECIMAL(20, 8)
);

-- 하이퍼테이블로 변환 (자동 파티셔닝)
SELECT create_hypertable('candles', 'time');

-- 인덱스 추가
CREATE INDEX idx_candles_symbol_time ON candles (symbol, time DESC);
```

#### 2. 데이터 삽입 (Kotlin 예시)

```kotlin
// 일반 PostgreSQL과 동일한 방식으로 사용
@Repository
class CandleRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun save(candle: Candle) {
        jdbcTemplate.update(
            """
            INSERT INTO candles (time, symbol, open_price, high_price, low_price, close_price, volume)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            candle.time,
            candle.symbol,
            candle.openPrice,
            candle.highPrice,
            candle.lowPrice,
            candle.closePrice,
            candle.volume
        )
    }
}
```

#### 3. 시계열 쿼리 예시

```sql
-- 최근 24시간 데이터 조회 (매우 빠름!)
SELECT * FROM candles
WHERE symbol = 'BTCUSDT'
  AND time >= NOW() - INTERVAL '24 hours'
ORDER BY time DESC;

-- 시간별 집계 (자동 최적화)
SELECT 
    time_bucket('1 hour', time) AS hour,
    symbol,
    AVG(close_price) AS avg_price,
    MAX(high_price) AS max_price,
    MIN(low_price) AS min_price,
    SUM(volume) AS total_volume
FROM candles
WHERE symbol = 'BTCUSDT'
  AND time >= NOW() - INTERVAL '7 days'
GROUP BY hour, symbol
ORDER BY hour DESC;

-- 이동평균 계산
SELECT 
    time,
    close_price,
    AVG(close_price) OVER (
        ORDER BY time 
        ROWS BETWEEN 19 PRECEDING AND CURRENT ROW
    ) AS sma_20
FROM candles
WHERE symbol = 'BTCUSDT'
ORDER BY time DESC
LIMIT 100;
```

---

## 성능 비교

### 일반 PostgreSQL vs TimescaleDB

| 작업 | PostgreSQL | TimescaleDB |
|------|-----------|------------|
| 1년치 데이터 삽입 (1분봉) | ~10분 | ~2분 |
| 24시간 데이터 조회 | ~500ms | ~50ms |
| 1개월 집계 쿼리 | ~2초 | ~200ms |
| 저장 공간 (1년치) | 10GB | 1GB (압축 후) |

---

## 자동 압축 설정

```sql
-- 압축 정책 설정 (30일 이상 된 데이터 자동 압축)
ALTER TABLE candles SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'symbol'
);

-- 압축 정책 추가
SELECT add_compression_policy('candles', INTERVAL '30 days');
```

**효과**: 
- 저장 공간 90% 절감
- 조회 성능 유지 (압축된 데이터도 빠르게 조회)

---

## 연속 집계 (Continuous Aggregates)

실시간으로 집계 데이터를 자동 유지:

```sql
-- 시간별 집계 뷰 생성
CREATE MATERIALIZED VIEW candles_hourly
WITH (timescaledb.continuous) AS
SELECT 
    time_bucket('1 hour', time) AS hour,
    symbol,
    AVG(close_price) AS avg_price,
    MAX(high_price) AS max_price,
    MIN(low_price) AS min_price,
    SUM(volume) AS total_volume
FROM candles
GROUP BY hour, symbol;

-- 자동 새로고침 정책 (1시간마다)
SELECT add_continuous_aggregate_policy('candles_hourly',
    start_offset => INTERVAL '3 hours',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour');
```

**장점**: 
- 대시보드 조회 시 즉시 응답
- 원본 데이터 조회 없이 집계 데이터만 조회

---

## Kotlin 통합 예시

### build.gradle.kts

```kotlin
dependencies {
    // PostgreSQL 드라이버 (TimescaleDB는 PostgreSQL 확장이므로 동일)
    runtimeOnly("org.postgresql:postgresql")
    
    // Spring Data JDBC
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}
```

### application.properties

```properties
# TimescaleDB 연결 (PostgreSQL과 동일한 설정)
spring.datasource.url=jdbc:postgresql://localhost:5432/trading_db
spring.datasource.username=trading_user
spring.datasource.password=trading_pass
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Repository 예시

```kotlin
@Repository
class CandleRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    // 데이터 저장
    fun save(candle: Candle) {
        jdbcTemplate.update(
            "INSERT INTO candles (time, symbol, open_price, high_price, low_price, close_price, volume) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            candle.time, candle.symbol, candle.openPrice, candle.highPrice,
            candle.lowPrice, candle.closePrice, candle.volume
        )
    }
    
    // 최근 N개 조회
    fun findRecent(symbol: String, limit: Int): List<Candle> {
        return jdbcTemplate.query(
            "SELECT * FROM candles WHERE symbol = ? ORDER BY time DESC LIMIT ?",
            { rs, _ ->
                Candle(
                    time = rs.getTimestamp("time").toInstant(),
                    symbol = rs.getString("symbol"),
                    openPrice = rs.getBigDecimal("open_price"),
                    highPrice = rs.getBigDecimal("high_price"),
                    lowPrice = rs.getBigDecimal("low_price"),
                    closePrice = rs.getBigDecimal("close_price"),
                    volume = rs.getBigDecimal("volume")
                )
            },
            symbol, limit
        )
    }
    
    // 시간 범위 조회
    fun findByTimeRange(
        symbol: String,
        startTime: Instant,
        endTime: Instant
    ): List<Candle> {
        return jdbcTemplate.query(
            "SELECT * FROM candles WHERE symbol = ? AND time >= ? AND time <= ? ORDER BY time",
            { rs, _ -> /* 매핑 로직 */ },
            symbol, Timestamp.from(startTime), Timestamp.from(endTime)
        )
    }
}
```

---

## Docker로 TimescaleDB 실행

### docker-compose.yml

```yaml
version: '3.8'

services:
  timescaledb:
    image: timescale/timescaledb:latest-pg15
    container_name: trading-timescaledb
    environment:
      POSTGRES_DB: trading_db
      POSTGRES_USER: trading_user
      POSTGRES_PASSWORD: trading_pass
    ports:
      - "5432:5432"
    volumes:
      - timescaledb-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U trading_user"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  timescaledb-data:
```

### 실행

```bash
docker-compose up -d
```

---

## 언제 사용해야 하나?

### ✅ TimescaleDB를 사용해야 하는 경우

- **시계열 데이터가 많을 때** (일일 수백만 건 이상)
- **시간 범위 조회가 빈번할 때**
- **과거 데이터 압축이 필요할 때**
- **실시간 집계가 필요할 때**

### ❌ 일반 PostgreSQL로 충분한 경우

- 데이터 양이 적을 때 (일일 수만 건 이하)
- 단순한 CRUD만 필요할 때
- 시계열 특화 기능이 필요 없을 때

---

## 트레이딩 시스템에서의 활용 시나리오

### 시나리오 1: 캔들스틱 데이터 저장

```
1분봉 데이터 × 24시간 × 365일 = 525,600건/년/코인
10개 코인 = 5,256,000건/년

→ TimescaleDB로 저장하면:
- 빠른 조회 (50ms 이내)
- 자동 압축 (1GB로 축소)
- 시간 범위 쿼리 최적화
```

### 시나리오 2: 실시간 차트 데이터

```sql
-- 대시보드에서 최근 1시간 데이터 조회
SELECT * FROM candles
WHERE symbol = 'BTCUSDT'
  AND time >= NOW() - INTERVAL '1 hour'
ORDER BY time;

-- TimescaleDB: ~10ms
-- 일반 PostgreSQL: ~500ms
```

### 시나리오 3: 기술적 지표 계산

```sql
-- RSI 계산을 위한 과거 데이터 조회
SELECT close_price 
FROM candles
WHERE symbol = 'BTCUSDT'
  AND time >= NOW() - INTERVAL '14 days'
ORDER BY time;

-- TimescaleDB: 빠른 조회로 실시간 계산 가능
```

---

## 마이그레이션 전략

### 단계별 도입

1. **Phase 1**: 새로운 시계열 데이터만 TimescaleDB에 저장
2. **Phase 2**: 기존 PostgreSQL 데이터를 TimescaleDB로 마이그레이션
3. **Phase 3**: 시계열 쿼리를 TimescaleDB로 전환

### 기존 PostgreSQL과 공존

```
PostgreSQL: 거래 이력, 설정, 사용자 정보
TimescaleDB: 캔들스틱, 가격 틱, 기술적 지표
Redis: 캐시
```

---

## 요약

**TimescaleDB는**:
- PostgreSQL 기반 시계열 데이터베이스
- 시간 기반 데이터에 최적화
- 자동 파티셔닝 및 압축
- 트레이딩 시스템의 캔들스틱 데이터에 적합

**트레이딩 시스템에서**:
- 캔들스틱 데이터 → TimescaleDB
- 거래 이력 → PostgreSQL
- 대시보드 캐시 → Redis

이렇게 계층화하여 각 저장소의 장점을 활용합니다!

