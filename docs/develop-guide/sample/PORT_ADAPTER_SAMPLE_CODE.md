# Port, Adapter Sample code

## 개요

각 모듈에서는 다른 모듈의 코드를 최대한 참조 안하기 위해 port, adapter 를 구현하여 참조하도록 구현해야 합니다.<br>
아래는 이러한 패턴으로 구현했을 때의 샘플 코드입니다.

```text
com.xijjun.trading
├── risk (Risk 모듈)
│   ├── api/                      # [External] 외부 노출 영역
│   │   ├── out/                  # Outbound Port (SPI)
│   │   │   ├── RiskOrderPort.kt  # 현재 모듈에 외부 모듈로 부터 필요한 기능 Spec 을 interface 로 정의 (구현체는 외부 모듈 infrastructure 에 존재)
│   │   │   └── RiskEvaluationTarget.kt (VO)
│   │   └── in/                   # Inbound Port (API)
│   │       └── RiskValidationUseCase.kt
│   └── internal/                 # [Internal] 캡슐화 영역
│       ├── domain/               # 비즈니스 로직
│       │   └── usecases/          # Domain Service Logic
│       │       └── RiskValidator.kt # 리스트 판단 로직
│       ├── model/                # JPA Entity class
│       │   └── RiskResult.kt
│       └── infrastructure/       # 외부 연동 구현 (현재는 비어있음)
│
└── trading (Trading 모듈)
    ├── api/                      # [External] 외부 노출 영역
    │   └── in/                   # Inbound Port (API)
    │       └── OrderUseCase.kt
    └── internal/                 # [Internal] 캡슐화 영역
        ├── domain/               # 비즈니스 로직
        │   └── usecases/          # Domain Service Logic
        │       └── OrderCreateService.kt # 포지션 주문을 요청하는 서비스 로직
        ├── model/                # JPA Entity class
        │   ├── OrderRepository.kt # repository interface (구현체는 infrastructure)
        │   └── Order.kt
        └── infrastructure/       # 기술적 세부 구현
            ├── persistence/      # DB 관련 (Repository)
            │   └── OrderRepositoryImpl.kt
            └── adapter/          # 타 모듈 Port의 실제 구현체
                └── TradingRiskAdapter.kt
```

### [1] Risk 모듈 (Port 정의 영역)

Risk 모듈은 외부 모듈의 존재를 몰라도 되며, 오직 자신이 필요한 데이터 규격만 정의합니다.

**파일 경로:** `com.xijjun.trading.risk.api.out.RiskOrderPort.kt`

```kotlin
package com.xijjun.trading.risk.api.out

import java.math.BigDecimal

/**
 * Outbound Port : Rist 모듈이 외부로부터 전달받을 데이터 spec 정의 (해당 클래스는 외부 모듈에서 참조 가능)
 */
data class RiskEvaluationTarget(
    val orderId: Long,
    val symbol: String,
    val price: BigDecimal,
    val quantity: BigDecimal
)

/**
 * Outbound Port: Risk 모듈이 외부로부터 데이터를 가져오기 위한 인터페이스
 */
interface RiskOrderPort {
    fun getOrderData(orderId: Long): RiskEvaluationTarget
}
```

### [2] Trading 모듈 (Adapter 구현 영역)

Trading 모듈은 내부 엔티티를 보호하면서, Risk 모듈이 요구하는 인터페이스를 구현하여 데이터를 제공합니다.

파일 경로: `com.xijjun.trading.trading.internal.model.Order.kt`

```kotlin
package com.xijjun.trading.trading.internal.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "orders")
internal class Order(
    @Id @GeneratedValue
    val id: Long = 0,
    val symbol: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val createdAt: ZoneTime
)
```

파일 경로: `com.xijjun.trading.trading.internal.infrastructure.adapter.TradingRiskAdapter.kt`

```kotlin
package com.xijjun.trading.trading.internal.infrastructure.adapter

import com.xijjun.trading.risk.api.out.RiskOrderPort
import com.xijjun.trading.risk.api.out.RiskEvaluationTarget
import com.xijjun.trading.trading.internal.model.OrderRepository
import org.springframework.stereotype.Component


@Component
internal class TradingRiskAdapter(
    private val orderRepository: OrderRepository
) : RiskOrderPort { // Risk 모듈의 External 포트를 여기서 구현

    override fun getOrderData(orderId: Long): RiskEvaluationTarget {
        val order = orderRepository.findById(orderId)
            .orElseThrow { NoSuchElementException("Order not found") }

        // 내부 Entity를 타 모듈의 외부 규격(VO)으로 변환
        return RiskEvaluationTarget(
            orderId = order.id,
            symbol = order.symbol,
            price = order.price,
            quantity = order.quantity
        )
    }
}
```

### [3] Risk 모듈 (비즈니스 로직 사용 예시)

Risk 모듈은 내부 로직에서 주입받은 Port를 사용하여 데이터를 처리합니다.

파일 경로: `com.xijjun.trading.risk.domain.usecases.RiskValidator.kt`

```kotlin
package com.xijjun.trading.risk.internal.domain.usecases

import com.xijjun.trading.risk.api.out.RiskOrderPort
import org.springframework.stereotype.Service

@Service
internal class RiskValidator(
    private val riskOrderPort: RiskOrderPort // 추상화된 interface 를 의존
) {
    fun validate(orderId: Long): Boolean {
        val target = riskOrderPort.getOrderData(orderId)
        val totalAmount = target.price * target.quantity
        return totalAmount < java.math.BigDecimal("10000000")
    }
}
```

## module dependency test
`src/test/kotlin/com/xijjun/trading/ArchitectureTest.kt`
```kotlin
package com.xijjun.trading

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ArchitectureTest {
    @Test
    fun verifyModulith() {
        // 모든 모듈의 의존성 관계와 캡슐화가 잘 지켜졌는지 검증
        ApplicationModules.of(TradingApplication::class.java).verify()
    }
}
```
