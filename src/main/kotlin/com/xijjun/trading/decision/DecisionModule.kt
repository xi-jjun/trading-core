package com.xijjun.trading.decision

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/**
 * Decision 모듈
 * 
 * 전략 실행 및 매매 신호(Signal) 생성을 담당합니다.
 * 
 * ## 의존성 규칙
 * - **허용된 의존성**: `common`, `analysis`만 의존 가능
 * - **금지된 의존성**: 다른 모듈 (marketdata, trading, risk, ui)
 * 
 * ## 모듈 구조
 * ```
 * common, analysis
 *   ↑
 * decision
 * ```
 * 
 * ## 이벤트
 * - 수신: AnalysisCompletedEvent (analysis 모듈)
 * - 발행: TradingSignalEvent (trading 모듈로)
 * 
 * @see com.xijjun.trading.common.CommonModule
 * @see com.xijjun.trading.analysis.AnalysisModule
 */
@ApplicationModule(
    type = ApplicationModule.Type.CLOSED  // 내부 구현은 다른 모듈에서 접근 불가
)
@PackageInfo
object DecisionModule

