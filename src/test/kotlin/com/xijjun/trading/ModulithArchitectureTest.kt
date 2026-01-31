package com.xijjun.trading

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

/**
 * Spring Modulith 아키텍처 검증 테스트
 * 
 * JUnit 5를 사용하여 모듈 구조와 의존성 규칙을 검증합니다.
 */
class ModulithArchitectureTest {

    private val modules = ApplicationModules.of(TradingCoreApplication::class.java)

    @Test
    fun `모듈 구조와 의존성 규칙이 올바르게 구성되어야 합니다`() {
        // modules.verify()는 모듈 구조, 의존성 규칙, 순환 의존성을 모두 검증합니다
        modules.verify()
    }

    @Test
    fun `모듈 구조 문서를 생성할 수 있어야 합니다`() {
        Documenter(modules)
            .writeDocumentation()
            .writeIndividualModulesAsPlantUml()
    }
}

