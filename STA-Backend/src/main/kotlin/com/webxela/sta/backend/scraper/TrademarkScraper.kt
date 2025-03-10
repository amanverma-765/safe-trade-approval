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

        val initialTmResponse: String?

        val firstPageResponse = httpClient.get(TRADEMARK_URL).bodyAsText()
        val firstPageFormData = payloadParser.getPayloadFromFirstPage(firstPageResponse)

        val secondPageResponse = retry(maxRetries, retryDelay) {
            httpClient.post(TRADEMARK_URL) {
                contentType(ContentType.MultiPart.FormData)
                setBody(firstPageFormData)
            }.bodyAsText()
        }
        val secondPageFormData = payloadParser.getPayloadFromSecondPage(appId, captcha, secondPageResponse)

        // Retry mechanism for the initial POST request
        initialTmResponse = retry(maxRetries, retryDelay) {
            httpClient.post(TRADEMARK_URL) {
                contentType(ContentType.MultiPart.FormData)
                setBody(secondPageFormData)
            }.bodyAsText()
        }

        if (!trademarkParser.checkIfOnRightPage(initialTmResponse)) {
            val errorMessage = "No Trademark found, Either Trademark number is invalid or doesn't exist"
            logger.error(errorMessage)
        }

        // Retry mechanism for the final POST request
        val finalResponse: String = retry(maxRetries, retryDelay) {
            val finalFormData = payloadParser.getPayloadForThirdPage(response = initialTmResponse)
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
