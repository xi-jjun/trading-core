package com.xijjun.trading.risk

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/**
 * Risk Management 모듈
 * 
 * 실시간 리스크 검증 및 방어선 역할을 담당합니다.
 * 
 * ## 의존성 규칙
 * - **허용된 의존성**: `common`만 의존 가능
 * - **금지된 의존성**: 다른 모든 모듈 (marketdata, analysis, decision, trading, ui)
 * 
 * ## 모듈 구조
 * ```
 * common
 *   ↑
 * risk
 * ```
 * 
 * ## 통신 방식
 * - Trading 모듈: 동기 Port 호출 (RiskCheck) - Trading 모듈에서 호출
 * 
 * @see com.xijjun.trading.common.CommonModule
 */
@ApplicationModule(
    type = ApplicationModule.Type.CLOSED
)
@PackageInfo
object RiskModule

