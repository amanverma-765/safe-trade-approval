package com.webxela.sta.backend.utils

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds


data class RetryConfig(
    val maxAttempts: Int = 10,
    val initialDelay: Long = 5,  // seconds
    val maxDelay: Long = 500,     // seconds
    val factor: Double = 2.0
)

suspend fun <T> retryWithExponentialBackoff(
    config: RetryConfig = RetryConfig(),
    operation: suspend () -> T
): T {
    var currentDelay = config.initialDelay
    var attemptCount = 0

    val logger = LoggerFactory.getLogger("RetryMechanism")

    while (true) {
        try {
            return operation()
        } catch (e: Exception) {
            attemptCount++
            if (attemptCount >= config.maxAttempts) {
                logger.error("Final attempt $attemptCount failed after ${config.maxAttempts} retries", e)
                throw RuntimeException("Operation failed after ${config.maxAttempts} attempts", e)
            }

            logger.warn("Attempt $attemptCount failed, retrying in $currentDelay seconds", e)
            delay(currentDelay.seconds)

            // Calculate next delay with exponential backoff
            currentDelay = (currentDelay * config.factor)
                .toLong()
                .coerceAtMost(config.maxDelay)
        }
    }
}