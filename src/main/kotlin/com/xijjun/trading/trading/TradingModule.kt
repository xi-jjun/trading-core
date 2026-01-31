package com.xijjun.trading.trading

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/**
 * Trading 모듈
 * 
 * 주문 집행 및 포지션 관리를 담당합니다.
 * 
 * ## 의존성 규칙
 * - **허용된 의존성**: `common`, `risk`만 의존 가능
 * - **금지된 의존성**: 다른 모듈 (marketdata, analysis, decision, ui)
 * 
 * ## 모듈 구조
 * ```
 * common, risk
 *   ↑
 * trading
 * ```
 * 
 * ## 이벤트
 * - 수신: TradingSignalEvent (decision 모듈)
 * - 발행: OrderExecutedEvent, PositionOpenedEvent 등
 * 
 * ## 통신 방식
 * - Risk 모듈: 동기 Port 호출 (RiskCheck)
 * - UI 모듈: 동기 Port 호출 (Read/Write)
 * 
 * @see com.xijjun.trading.common.CommonModule
 * @see com.xijjun.trading.risk.RiskModule
 */
@ApplicationModule(
    type = ApplicationModule.Type.CLOSED  // 내부 구현은 다른 모듈에서 접근 불가
)
@PackageInfo
object TradingModule

