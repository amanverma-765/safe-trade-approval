package com.webxela.sta.backend.scraper

import com.webxela.sta.backend.utils.Constants.GET_CAPTCHA_URL
import com.webxela.sta.backend.utils.Header.getDefaultHeaders
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

    suspend fun requestCaptcha(httpClient: HttpClient): String? {
        val payload = "{}"
        return try {
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
                null
            }
        } catch (ex: Exception) {
            logger.error("Exception occurred: ${ex.message}")
            throw ex
        }
    }

    @Serializable
    data class CaptchaResponse(val d: String)

}