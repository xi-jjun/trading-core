# Spring Modulith 전략 및 모듈 구조

## Spring Modulith란?

**Spring Modulith**는 단일 애플리케이션(Monolith) 내에서 모듈을 논리적으로 분리하는 프레임워크입니다.

### 핵심 개념

```
단일 애플리케이션 (Monolith)
├── collection 모듈 (패키지)
├── processing 모듈 (패키지)
├── trading 모듈 (패키지)
├── risk 모듈 (패키지)
├── ai 모듈 (패키지)
└── api 모듈 (패키지)
```

**특징**:
- ✅ **단일 JAR 파일**: 하나의 애플리케이션으로 배포
- ✅ **패키지 기반 분리**: 각 모듈은 패키지로 분리
- ✅ **의존성 검증**: 모듈 간 의존성 규칙 자동 검증
- ✅ **마이크로서비스 전환 가능**: 나중에 독립 서비스로 분리 가능

---

## 왜 Spring Modulith를 선택했나?

### MVP 단계에서의 장점

#### 1. 단순한 배포 및 운영
```
일반 마이크로서비스:
- 6개 서비스 배포
- 6개 서비스 모니터링
- 6개 서비스 로그 관리
- 네트워크 통신 관리

Spring Modulith (Monolith):
- 1개 애플리케이션 배포
- 1개 애플리케이션 모니터링
- 1개 애플리케이션 로그
- 인메모리 통신 (빠름)
```

**MVP 장점**: 빠른 개발, 간단한 운영, 낮은 복잡도

#### 2. 명확한 모듈 경계
```kotlin
// 패키지 구조
com.xijjun.trading
├── collection          // 데이터 수집 모듈
│   ├── internal       // 내부 구현 (다른 모듈에서 접근 불가)
│   └── CandlestickCollector  // 공개 API
├── processing         // 데이터 가공 모듈
│   ├── internal
│   └── TechnicalIndicatorCalculator
├── trading            // 코인 매매 모듈
│   ├── internal
│   └── OrderExecutor
├── risk               // 리스크 관리 모듈
│   ├── internal
│   └── RiskValidator
├── ai                 // AI 모듈
│   ├── internal
│   └── TradingSignalGenerator
└── api                // API Logic 모듈
    └── controller
```

**장점**: 모듈 간 경계가 명확하고, 의존성 규칙을 자동으로 검증

#### 3. 개발 속도 향상
- 모듈 간 통신이 인메모리 호출 (네트워크 오버헤드 없음)
- 디버깅이 쉬움 (단일 프로세스)
- 테스트가 간단함 (통합 테스트 용이)

#### 4. 마이크로서비스로의 전환 가능
```
Phase 1 (MVP): Monolith (Spring Modulith)
    ↓
Phase 2: 필요 시 특정 모듈만 분리
    ↓
Phase 3: 완전한 마이크로서비스 (선택)
```

**장점**: 나중에 필요하면 점진적으로 분리 가능

---

## 모듈 구조 상세

### 패키지 구조

```
src/main/kotlin/com/xijjun/trading/
├── collection/                    # 데이터 수집 모듈
│   ├── internal/                 # 내부 구현 (다른 모듈에서 접근 불가)
│   │   ├── BinanceApiClient.kt
│   │   └── CandlestickCollector.kt
│   └── CandlestickCollectionService.kt  # 공개 API
│
├── processing/                    # 데이터 가공 모듈
│   ├── internal/
│   │   ├── SmaCalculator.kt
│   │   └── RsiCalculator.kt
│   └── TechnicalIndicatorService.kt
│
├── trading/                        # 코인 매매 모듈
│   ├── internal/
│   │   ├── BinanceOrderClient.kt
│   │   └── OrderExecutor.kt
│   └── TradingService.kt
│
├── risk/                          # 리스크 관리 모듈
│   ├── internal/
│   │   └── RiskValidatorImpl.kt
│   └── RiskValidator.kt          # 공개 인터페이스
│
├── ai/                            # AI 모듈
│   ├── internal/
│   │   └── RuleBasedSignalGenerator.kt
│   └── TradingSignalGenerator.kt
│
└── api/                           # API Logic 모듈
    ├── controller/
    │   ├── PositionController.kt
    │   ├── TradeController.kt
    │   └── ConfigController.kt
    └── ApiApplication.kt          # 메인 애플리케이션
```

### 모듈 간 의존성 규칙

```kotlin
// ✅ 허용되는 의존성
api → collection, processing, trading, risk, ai
trading → risk, ai
ai → processing
processing → collection (간접, DB를 통해서만)

// ❌ 금지되는 의존성
collection → trading  // 순환 의존성
risk → trading        // 순환 의존성
processing → trading // 순환 의존성
```

**Spring Modulith가 자동으로 검증**:
- 컴파일 타임에 의존성 규칙 검증
- 테스트에서 모듈 경계 검증
- 문서 자동 생성

---

## 모듈 간 통신 방식

### 1. 직접 호출 (인메모리) - MVP

```kotlin
// trading 모듈에서 risk 모듈 호출
@Service
class TradingService(
    private val riskValidator: RiskValidator  // 직접 주입
) {
    fun executeOrder(order: Order) {
        val validation = riskValidator.validate(order)
        if (validation.approved) {
            // 주문 실행
        }
    }
}
```

**장점**: 빠름, 단순함, 타입 안전

### 2. 이벤트 기반 (Phase 2 이후)

```kotlin
// Spring Events 사용
@EventListener
class TradingService {
    @EventListener
    fun handleSignal(signal: TradingSignal) {
        // 매매 신호 처리
    }
}

// AI 모듈에서 이벤트 발행
applicationEventPublisher.publishEvent(TradingSignal(...))
```

**장점**: 느슨한 결합, 비동기 처리 가능

### 3. 마이크로서비스로 분리 시 (Phase 3)

```kotlin
// HTTP 클라이언트로 변경
@Service
class TradingService(
    private val riskServiceClient: RiskServiceClient  // HTTP 클라이언트
) {
    fun executeOrder(order: Order) {
        val validation = riskServiceClient.validate(order)
        // ...
    }
}
```

---

## 모듈 정의 예시

### collection 모듈

```kotlin
// collection/internal/CandlestickCollector.kt
@Modulith.Module
internal class CandlestickCollector {
    fun collect(symbol: String): List<Candlestick> {
        // 바이낸스 API 호출
    }
}

// collection/CandlestickCollectionService.kt
@Service
class CandlestickCollectionService(
    private val collector: CandlestickCollector
) {
    fun collectAndSave(symbol: String) {
        val candles = collector.collect(symbol)
        // DB 저장
    }
}
```

### risk 모듈 (공개 API)

```kotlin
// risk/RiskValidator.kt
interface RiskValidator {
    fun validate(order: Order): RiskValidationResult
}

// risk/internal/RiskValidatorImpl.kt
@Service
class RiskValidatorImpl : RiskValidator {
    override fun validate(order: Order): RiskValidationResult {
        // 리스크 검증 로직
    }
}
```

### trading 모듈 (risk 모듈 사용)

```kotlin
// trading/TradingService.kt
@Service
class TradingService(
    private val riskValidator: RiskValidator  // 인터페이스 주입
) {
    fun executeOrder(order: Order) {
        val validation = riskValidator.validate(order)
        // ...
    }
}
```

---

## 모듈 테스트

### 단위 테스트 (모듈 내부)

```kotlin
// collection 모듈 테스트
@SpringBootTest
class CandlestickCollectorTest {
    @Test
    fun `should collect candlestick data`() {
        // 테스트
    }
}
```

### 모듈 통합 테스트

```kotlin
// Spring Modulith 테스트 지원
@ModulithTest
class TradingModuleTest {
    @Test
    fun `should validate module structure`() {
        // 모듈 구조 자동 검증
    }
}
```

### E2E 테스트

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class TradingE2ETest {
    @Test
    fun `should execute full trading flow`() {
        // 전체 플로우 테스트
    }
}
```

---

## 마이크로서비스로의 전환 전략

### Phase 1: Monolith (MVP)
```
단일 애플리케이션
├── collection
├── processing
├── trading
├── risk
├── ai
└── api
```

### Phase 2: 점진적 분리 (필요 시)
```
애플리케이션 1: collection, processing
애플리케이션 2: trading, risk
애플리케이션 3: ai
애플리케이션 4: api
```

### Phase 3: 완전한 마이크로서비스 (선택)
```
서비스 1: collection-service
서비스 2: processing-service
서비스 3: trading-service
서비스 4: risk-service
서비스 5: ai-service
서비스 6: api-gateway
```

**전환 방법**:
1. 모듈을 독립 프로젝트로 분리
2. HTTP 클라이언트로 통신 변경
3. 서비스 디스커버리 추가 (선택)
4. API Gateway 추가 (선택)

---

## Spring Modulith 설정

### build.gradle.kts

```kotlin
dependencies {
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
}
```

### 모듈 정의

```kotlin
// src/main/kotlin/com/xijjun/trading/api/ApiApplication.kt
@SpringBootApplication
@Modulith
class TradingCoreApplication {
    fun main(args: Array<String>) {
        runApplication<TradingCoreApplication>(*args)
    }
}
```

### 모듈 문서 자동 생성

```kotlin
// 테스트에서 모듈 문서 생성
@ModulithTest
class ModuleDocumentationTest {
    @Test
    fun generateModuleDocumentation() {
        // 모듈 구조 문서 자동 생성
    }
}
```

---

## MVP에서의 이점 요약

### ✅ 개발 측면
- 빠른 개발 속도 (단일 프로젝트)
- 쉬운 디버깅 (단일 프로세스)
- 간단한 테스트 (통합 테스트 용이)

### ✅ 운영 측면
- 단순한 배포 (1개 JAR)
- 쉬운 모니터링 (1개 애플리케이션)
- 낮은 인프라 비용

### ✅ 아키텍처 측면
- 명확한 모듈 경계
- 자동 의존성 검증
- 마이크로서비스 전환 가능

---

## 결론

**Spring Modulith는 MVP에 최적화된 선택입니다:**

1. **단일 모듈(Monolith)로 시작**: 빠른 개발, 간단한 운영
2. **논리적 모듈 분리**: 명확한 경계, 자동 검증
3. **확장 가능**: 필요 시 마이크로서비스로 전환

**MVP 단계에서는**:
- ✅ 단일 애플리케이션으로 모든 모듈 포함
- ✅ 패키지 기반으로 모듈 분리
- ✅ 인메모리 통신으로 빠른 개발

**Phase 2 이후**:
- 필요 시 특정 모듈만 분리
- 이벤트 기반 통신 도입
- 완전한 마이크로서비스 전환 (선택)

이 방식으로 **"빠르게 시작하고, 필요 시 확장"**하는 전략을 구현할 수 있습니다.

