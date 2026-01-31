package com.xijjun.trading

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.modulith.Modulith

@Modulith
@SpringBootApplication
class TradingCoreApplication

fun main(args: Array<String>) {
    runApplication<TradingCoreApplication>(*args)
}
