package com.xijjun.trading.ui

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/**
 * User Interface 모듈
 * 
 * REST API 및 실시간 알림을 제공합니다.
 * 
 * ## 의존성 규칙
 * - **허용된 의존성**: 모든 모듈 (common, marketdata, analysis, decision, trading, risk)
 * - **금지된 의존성**: 없음 (최상위 레이어)
 * 
 * ## 모듈 구조
 * ```
 * 모든 모듈
 *   ↑
 * ui (최상위)
 * ```
 * 
 * ## 통신 방식
 * - Trading 모듈: 동기 Port 호출 (Read/Write)
 * - Analysis 모듈: 동기 Port 호출 (Read)
 * - 다른 모듈: 필요 시 동기 Port 호출
 * 
 * @see com.xijjun.trading.common.CommonModule
 * @see com.xijjun.trading.marketdata.MarketDataModule
 * @see com.xijjun.trading.analysis.AnalysisModule
 * @see com.xijjun.trading.decision.DecisionModule
 * @see com.xijjun.trading.trading.TradingModule
 * @see com.xijjun.trading.risk.RiskModule
 */
@ApplicationModule(
    type = ApplicationModule.Type.OPEN  // 모든 모듈에 접근 가능하도록 OPEN
)
@PackageInfo
object UiModule

