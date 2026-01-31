package com.xijjun.trading.modules

import com.xijjun.trading.TradingCoreApplication
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

/**
 * Trading 모듈 검증 테스트
 */
class TradingModuleTest {

    private val modules = ApplicationModules.of(TradingCoreApplication::class.java)

    @Test
    fun `Trading 모듈이 올바르게 구성되어야 합니다`() {
        modules.verify()
    }
}

