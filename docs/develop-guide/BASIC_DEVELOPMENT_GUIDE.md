# 🚀 Trading Core System Architecture Guide

이 프로젝트는 **Spring Boot 4**, **Kotlin**, **Spring Modulith**를 기반으로 하며, 도메인 주도 설계(DDD)의 **Bounded Context** 원칙을 따르는 모듈형 모놀리스(Modular Monolith) 구조입니다.

---

## 🏗️ 1. 핵심 설계 원칙

1. **관심사의 분리 (Separation of Concerns)**
    - 각 모듈은 독립적인 비즈니스 로직과 데이터베이스 테이블(Entity)을 소유합니다.
2. **의존성 역전 (Dependency Inversion)**
    - 모듈 간의 직접적인 결합을 피하기 위해 **Port(Interface)**를 정의하고 **Adapter**를 통해 구현합니다.
3. **엄격한 캡슐화 (Encapsulation)**
    - Kotlin의 `internal` 키워드를 사용하여 모듈 내부의 상세 구현(Entity, Repository)을 외부로부터 숨깁니다.

---

## 📁 2. 표준 패키지 구조 (Module Layout)

각 도메인 모듈은 아래의 구조를 반드시 준수해야 합니다.

```text
com.xijjun.trading.<module_name>
├── port/                 # [PUBLIC] 타 모듈과 소통하는 접점
│   ├── in/               # API: 외부에서 이 모듈을 호출하는 인터페이스 (UseCase)
│   └── out/              # SPI: 이 모듈이 외부 데이터를 필요로 할 때 정의하는 인터페이스
│       └── model/        # Port에서 사용하는 전용 DTO 및 VO (Data Class)
│
├── domain/               # [INTERNAL] 순수 비즈니스 로직 (Service, Domain Model)
│
└── internal/             # [INTERNAL] 상세 구현 영역 (외부 참조 불가)
    ├── entity/           # JPA Entity (해당 모듈 전용)
    ├── repository/       # Spring Data Repository
    └── adapter/          # Outbound Port의 실제 구현체 (타 모듈 데이터 매핑)
```

### 가시성 규칙 (Visibility)
- port/ 하위: public으로 선언하여 다른 모듈에서 참조 가능.
- internal/, domain/ 하위: 반드시 internal 키워드를 사용하여 타 모듈의 import를 차단.


## 🔄 3. 모듈 간 데이터 통신 가이드
### 상황 A: 동기 데이터 조회 (Port/Adapter)
- 방식: 데이터를 요청하는 모듈이 port.out에 인터페이스를 정의하고, 데이터를 가진 모듈이 internal.adapter에서 이를 구현합니다.
- 장점: 요청 모듈은 제공 모듈의 엔티티 구조를 몰라도 되며, 자신만의 VO로 데이터를 받습니다.

### 상황 B: 비동기 상태 전파 (Event Driven)
- 방식: 상태가 변경된 모듈이 이벤트를 발행(ApplicationEventPublisher)하고, 관심 있는 모듈이 이를 구독(@ApplicationModuleListener)합니다.
- 장점: 발행 모듈과 구독 모듈 간의 결합도를 0에 가깝게 유지합니다.

## 🛡️ 4. 엔티티 및 공유 모델 규칙
1. 엔티티 공유 금지 
   - 한 모듈의 @Entity를 다른 모듈에서 import 하는 것은 아키텍처 위반입니다.
2. Shared Kernel (공유 커널)
   - `com.xijjun.trading.shared` 패키지에는 `Price`, `Quantity`, `Symbol` 같이 전 도메인에서 공통으로 쓰이는 **불변 값 객체(VO)** 만 위치합니다.
3. 데이터 매핑
   - 모듈 경계를 넘을 때는 반드시 각 모듈의 Port에 정의된 전용 모델(Data Class)로 변환하여 전달합니다.

## ✅ 5. 아키텍처 검증 테스트
Spring Modulith 테스트를 통해 위 규칙 위반 여부를 자동으로 검증합니다.
```kotlin
@Test
fun verifyModulith() {
    val modules = ApplicationModules.of(TradingApplication::class.java)
    modules.verify() // 순환 참조 및 캡슐화 위반 시 빌드 실패
}
```

📝 6. 모듈 리스트 (Bounded Contexts)
모듈명	책임 범위
- marketdata	외부 거래소 데이터 수집 및 시계열 데이터 관리
- analysis	기술적 지표 계산 및 차트 패턴 분석
- decision	AI 기반 전략 실행 및 매매 신호 생성
- trading	주문 실행, 체결 관리, 포지션 추적
- risk	리스크 한도 검증 및 손절/익절 정책 관리
- ui	사용자 REST API 제공 및 웹소켓 알림
