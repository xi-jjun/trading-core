package com.xijjun.trading.analysis

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/**
 * Analysis 모듈
 * 
 * 기술적 지표 계산 및 데이터 가공을 담당합니다.
 * 
 * ## 의존성 규칙
 * - **허용된 의존성**: `common`, `marketdata`만 의존 가능
 * - **금지된 의존성**: 다른 모듈 (decision, trading, risk, ui)
 * 
 * ## 모듈 구조
 * ```
 * common, marketdata
 *   ↑
 * analysis
 * ```
 * 
 * ## 이벤트
 * - 수신: MarketDataCollectedEvent (marketdata 모듈)
 * - 발행: AnalysisCompletedEvent (decision 모듈로)
 * 
 * @see com.xijjun.trading.common.CommonModule
 * @see com.xijjun.trading.marketdata.MarketDataModule
 */
@ApplicationModule(
    type = ApplicationModule.Type.CLOSED  // 내부 구현은 다른 모듈에서 접근 불가
)
@PackageInfo
object AnalysisModule

