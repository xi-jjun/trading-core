package com.xijjun.trading.tradingcore

import com.xijjun.trading.config.TestContainerConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig::class) // 전역 설정 클래스 로드
class TradingCoreApplicationTests {

    @Test
    fun contextLoads() {
    }

}
