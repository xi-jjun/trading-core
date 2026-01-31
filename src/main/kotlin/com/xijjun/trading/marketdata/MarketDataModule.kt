package com.xijjun.trading.marketdata

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/**
 * Market Data 모듈
 * 
 * 외부 거래소 데이터 수집 및 정규화를 담당합니다.
 * 
 * ## 의존성 규칙
 * - **허용된 의존성**: `common`만 의존 가능
 * - **금지된 의존성**: 다른 모든 모듈 (analysis, decision, trading, risk, ui)
 * 
 * ## 모듈 구조
 * ```
 * common
 *   ↑
 * marketdata
 * ```
 * 
 * ## 이벤트 발행
 * - MarketDataCollectedEvent: 데이터 수집 완료 시 Analysis 모듈로 발행
 * 
 * @see com.xijjun.trading.common.CommonModule
 */
@ApplicationModule(
    type = ApplicationModule.Type.CLOSED  // 내부 구현은 다른 모듈에서 접근 불가
)
@PackageInfo
object MarketDataModule

