package com.xijjun.trading.common

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/**
 * Common 모듈
 * 
 * 다른 모듈들이 공통으로 사용하는 유틸리티 클래스들을 포함합니다.
 * 
 * ## 의존성 규칙
 * - **의존성 없음**: 다른 모듈에 의존하지 않음 (최하위 레이어)
 * - **다른 모듈에서 사용 가능**: 모든 모듈이 common을 사용할 수 있음
 * 
 * ## 모듈 구조
 * ```
 * common (최하위)
 *   ↑
 *   모든 모듈이 common 사용 가능
 * ```
 * 
 * @see com.xijjun.trading.marketdata.MarketDataModule
 * @see com.xijjun.trading.analysis.AnalysisModule
 * @see com.xijjun.trading.decision.DecisionModule
 * @see com.xijjun.trading.trading.TradingModule
 * @see com.xijjun.trading.risk.RiskModule
 * @see com.xijjun.trading.ui.UiModule
 */
@ApplicationModule(
    type = ApplicationModule.Type.OPEN  // 다른 모듈에서 접근 가능
)
@PackageInfo
object CommonModule

