package com.webxela.sta.backend

import com.webxela.sta.backend.utils.DotEnvInitializer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
class StaBackendApplication

fun main(args: Array<String>) {
	SpringApplicationBuilder(StaBackendApplication::class.java)
		.initializers(DotEnvInitializer())
		.run(*args)
}
