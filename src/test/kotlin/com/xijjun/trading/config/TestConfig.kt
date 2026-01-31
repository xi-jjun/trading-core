package com.xijjun.trading.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer

class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)

    override suspend fun beforeProject() {
        testDBContainer.start()
        // Spring Boot가 이 정보를 읽을 수 있도록 시스템 속성에 주입
        System.setProperty("spring.datasource.url", testDBContainer.jdbcUrl)
        System.setProperty("spring.datasource.username", testDBContainer.username)
        System.setProperty("spring.datasource.password", testDBContainer.password)
        System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver")
    }

    override suspend fun afterProject() {
        testDBContainer.stop()
    }

    companion object {
        val testDBContainer = PostgreSQLContainer("postgres:17-alpine").apply {
            withDatabaseName("postgres")
            withUsername("postgres")
            withPassword("testpassword")
        }
    }
}
