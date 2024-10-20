package com.webxela.sta.backend.scraper

import com.webxela.sta.backend.scraper.parser.PayloadParser
import com.webxela.sta.backend.scraper.parser.TrademarkParser
import com.webxela.sta.backend.services.TrademarkService
import com.webxela.sta.backend.utils.Constants.TRADEMARK_URL
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlinx.coroutines.delay

@Component
class TrademarkScraper(
    private val payloadParser: PayloadParser,
    private val trademarkParser: TrademarkParser
) {

    private val logger = LoggerFactory.getLogger(TrademarkService::class.java)

    // Retry parameters
    private val maxRetries = 4 // Maximum number of retries
    private val retryDelay = 2000L // Delay between retries (in milliseconds)

    suspend fun requestTrademarkData(
        httpClient: HttpClient,
        appId: String,
        captcha: String
    ): String? {

        val initialResponse: String?
        val finalResponse: String?

        val initialFormData = payloadParser.getStaticFormData(appId, captcha)

        // Retry mechanism for the initial POST request
        initialResponse = retry(maxRetries, retryDelay) {
            httpClient.post(TRADEMARK_URL) {
                contentType(ContentType.MultiPart.FormData)
                setBody(initialFormData)
            }.bodyAsText()
        }

        if (!trademarkParser.checkIfOnRightPage(initialResponse)) {
            val errorMessage = "No Trademark found, Either Trademark number is invalid or doesn't exist"
            logger.error(errorMessage)
        }

        // Retry mechanism for the final POST request
        finalResponse = retry(maxRetries, retryDelay) {
            val finalFormData = payloadParser.parsePayloadToFormData(response = initialResponse)
            httpClient.post(TRADEMARK_URL) {
                contentType(ContentType.MultiPart.FormData)
                setBody(finalFormData)
            }.bodyAsText()
        }

        logger.info("Extraction completed for $appId")
        return finalResponse
    }

    // Retry function
    private suspend fun <T> retry(maxRetries: Int, delayMillis: Long, block: suspend () -> T): T {
        var currentAttempt = 0
        var lastError: Throwable? = null
        while (currentAttempt < maxRetries) {
            try {
                return block()
            } catch (ex: Exception) {
                currentAttempt++
                lastError = ex
                logger.warn("Attempt $currentAttempt failed: ${ex.message}. Retrying in $delayMillis ms...")
                delay(delayMillis)
            }
        }
        throw lastError ?: IllegalStateException("Unknown error during retry")
    }
}
