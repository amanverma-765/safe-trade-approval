package com.webxela.sta.backend.scraper

import com.webxela.sta.backend.utils.Constants.CAPTCHA_URL
import com.webxela.sta.backend.utils.Constants.GET_CAPTCHA_URL
import com.webxela.sta.backend.utils.Constants.TRADEMARK_URL
import com.webxela.sta.backend.utils.Header.getDefaultHeaders
import com.webxela.sta.backend.utils.retryWithExponentialBackoff
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class TmCaptchaScraper {

    private val logger = LoggerFactory.getLogger(TmCaptchaScraper::class.java)

    // Retry parameters
    private val maxRetries = 5 // Maximum number of retries
    private val initialRetryDelay = 120000L // Initial delay (2 min)
    private val maxRetryDelay = 600000L // Delay between retries (10 min)

    suspend fun requestCaptcha(httpClient: HttpClient): String {
        val payload = "{}"
        logger.info("Fetching new CAPTCHA...")

        return retryWithExponentialBackoff(maxRetries, initialRetryDelay, maxRetryDelay) {

            httpClient.get(CAPTCHA_URL) { headers { getDefaultHeaders() } }
            httpClient.get(TRADEMARK_URL) { headers { getDefaultHeaders() } }

            val response = httpClient.post(GET_CAPTCHA_URL) {
                contentType(ContentType.Application.Json)
                setBody(payload)
                headers { getDefaultHeaders() }
            }

            if (response.status == HttpStatusCode.OK) {
                val jsonResponse: CaptchaResponse = response.body()
                logger.info("Captcha Request was successful! $jsonResponse")
                jsonResponse.d
            } else {
                logger.error("Failed with status code: ${response.status.value}")
                logger.error("Response text: ${response.bodyAsText()}")
                throw RuntimeException("Failed with status code: ${response.status.value}, retrying...")
            }
        }
    }

    @Serializable
    data class CaptchaResponse(val d: String)

}