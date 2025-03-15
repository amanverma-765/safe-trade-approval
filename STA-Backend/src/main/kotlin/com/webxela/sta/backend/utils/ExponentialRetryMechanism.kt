package com.webxela.sta.backend.utils

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory

// Retry function with exponential backoff
suspend fun <T> retryWithExponentialBackoff(
    maxRetries: Int,
    initialDelayMillis: Long,
    maxRetryDelay: Long,
    block: suspend () -> T
): T {

    val logger = LoggerFactory.getLogger("RetryWithExponentialBackoff")

    var currentAttempt = 0
    var lastError: Throwable? = null

    while (currentAttempt < maxRetries) {
        try {
            return block()
        } catch (ex: Exception) {
            currentAttempt++
            lastError = ex

            if (currentAttempt >= maxRetries) break

            // Calculate exponential backoff with jitter
            val exponentialDelay = initialDelayMillis * (1 shl (currentAttempt - 1))
            val jitter = (Math.random() * 0.3 * exponentialDelay).toLong()
            val delayWithJitter = (exponentialDelay + jitter).coerceAtMost(maxRetryDelay)

            logger.warn("Attempt $currentAttempt failed: ${ex.message}. Retrying in ${delayWithJitter}ms...")
            delay(delayWithJitter)
        }
    }
    throw IllegalStateException("Operation failed after $maxRetries attempts: ${lastError?.message}", lastError)
}