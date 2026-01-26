package com.runnershi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class RunnersHiBackendApplication

fun main(args: Array<String>) {
    runApplication<RunnersHiBackendApplication>(*args)
}
