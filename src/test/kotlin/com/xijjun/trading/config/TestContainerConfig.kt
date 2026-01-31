package com.xijjun.trading.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestContainerConfig {
    @Bean
    @ServiceConnection
    fun postgresContainer() = PostgreSQLContainer("postgres:17-alpine").apply {
        start()
    }
}
