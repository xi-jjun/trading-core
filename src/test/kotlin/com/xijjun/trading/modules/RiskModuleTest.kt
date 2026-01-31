package com.xijjun.trading.modules

import com.xijjun.trading.TradingCoreApplication
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

/**
 * Risk 모듈 검증 테스트
 */
class RiskModuleTest {

    private val modules = ApplicationModules.of(TradingCoreApplication::class.java)

    @Test
    fun `Risk 모듈이 올바르게 구성되어야 합니다`() {
        modules.verify()
    }
}

