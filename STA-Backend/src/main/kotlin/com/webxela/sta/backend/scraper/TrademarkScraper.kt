package com.webxela.sta.backend.scraper

import com.webxela.sta.backend.scraper.parser.PayloadParser
import com.webxela.sta.backend.scraper.parser.TrademarkParser
import com.webxela.sta.backend.services.TrademarkService
import com.webxela.sta.backend.utils.Constants.TRADEMARK_URL
import com.webxela.sta.backend.utils.Header.getDefaultHeaders
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
    private val maxRetries = 5 // Maximum number of retries
    private val initialRetryDelay = 120000L // Initial delay (2 min)
    private val maxRetryDelay = 900000L // Delay between retries (15 min)

    suspend fun requestTrademarkData(
        httpClient: HttpClient,
        appId: String,
        captcha: String
    ): String? {

        val finalResponse = retry(maxRetries, initialRetryDelay) {

            logger.info("Extraction started for $appId")

            val firstPageResponse = httpClient.get(TRADEMARK_URL) { headers { getDefaultHeaders() } }
            if (firstPageResponse.status != HttpStatusCode.OK) {
                if (
                    firstPageResponse.status == HttpStatusCode.InternalServerError
                    || firstPageResponse.status == HttpStatusCode.Unauthorized
                ) {
                    throw IllegalStateException("Blocked by server, exiting...")
                }
                logger.error("Failed to fetch Trademark data, getting status code ${firstPageResponse.status.value} ")
                return@retry null
            }
            val firstPageFormData = payloadParser.getPayloadFromFirstPage(firstPageResponse.bodyAsText())

            val secondPageResponse = httpClient.post(TRADEMARK_URL) {
                contentType(ContentType.MultiPart.FormData)
                setBody(firstPageFormData)
                headers { getDefaultHeaders() }
            }.bodyAsText()

            val secondPageFormData = payloadParser.getPayloadFromSecondPage(appId, captcha, secondPageResponse)

            val initialTmResponse = httpClient.post(TRADEMARK_URL) {
                contentType(ContentType.MultiPart.FormData)
                setBody(secondPageFormData)
                io.ktor.http.headers { getDefaultHeaders() }
            }.bodyAsText()


            if (!trademarkParser.checkIfOnRightPage(initialTmResponse)) {
                val errorMessage = "No Trademark found, Either Trademark id: $appId is invalid or doesn't exist"
                logger.error(errorMessage)
                return@retry null
            }

            // Retry mechanism for the final POST request
            val finalFormData = payloadParser.getPayloadForThirdPage(response = initialTmResponse)
            httpClient.post(TRADEMARK_URL) {
                contentType(ContentType.MultiPart.FormData)
                setBody(finalFormData)
                io.ktor.http.headers { getDefaultHeaders() }
            }.bodyAsText()
        }
        logger.info("Extraction completed for $appId")
        return finalResponse
    }

    // Retry function with exponential backoff
    private suspend fun <T> retry(maxRetries: Int, initialDelayMillis: Long, block: suspend () -> T): T {
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
}
