package com.webxela.sta.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
class AsyncConfig {

    @Bean(name = ["scraperExecutor"])
    fun scraperExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 100  // You can adjust this value
        executor.maxPoolSize = 200   // You can adjust this value
        executor.queueCapacity = 500
        executor.setThreadNamePrefix("ScraperThread-")
        executor.initialize()
        return executor
    }
}
