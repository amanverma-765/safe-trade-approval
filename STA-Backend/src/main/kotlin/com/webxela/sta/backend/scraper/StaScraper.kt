package com.webxela.sta.backend.scraper

import com.webxela.sta.backend.config.KtorClientConfig
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.scraper.parser.TrademarkParser
import com.webxela.sta.backend.utils.Constants.CAPTCHA_URL
import com.webxela.sta.backend.utils.Constants.MAX_THREADS
import com.webxela.sta.backend.utils.Constants.TRADEMARK_URL
import com.webxela.sta.backend.utils.Header.getDefaultHeaders
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors

@Component
class StaScraper(
    private val httpClientConfig: KtorClientConfig,
    private val tmCaptchaScraper: TmCaptchaScraper,
    private val trademarkScraper: TrademarkScraper,
    private val trademarkParser: TrademarkParser
) {
    private val logger = LoggerFactory.getLogger(StaScraper::class.java)

    // Scrape trademark by application ID
    suspend fun scrapeByAppId(appId: String): Trademark? {
        val trademark: Trademark?
        try {
            val httpClient = httpClientConfig.createHttpClient()
            val captcha = tmCaptchaScraper.requestCaptcha(httpClient)
            trademark = scrapeTrademark(httpClient = httpClient, appId = appId, captcha = captcha)
        } catch (ex: Exception) {
            logger.error("Error while scraping by application id ${ex.message}")
            throw ex
        }
        return trademark
    }

    // Scrape trademark data
    private suspend fun scrapeTrademark(
        httpClient: HttpClient,
        appId: String,
        captcha: String
    ): Trademark? {
        var tmData: Trademark? = null
        try {
            val tmResponse = trademarkScraper.requestTrademarkData(httpClient, appId, captcha)
            tmResponse?.let { response ->
                tmData = trademarkParser.parseTrademarkDetails(response, httpClient)
            }
        } catch (ex: Exception) {
            logger.error("Error while scraping application id $appId: ${ex.message}")
            throw ex
        }
        return tmData
    }

    // Scrape trademarks by journal file path
    suspend fun scrapeTrademarkByList(
        applicationNumberList: List<String>,
        threadCount: Int = MAX_THREADS
    ): List<Trademark> {
        val tData = mutableListOf<Trademark>()
        val chunks: List<List<String>> = if (applicationNumberList.size <= threadCount) {
            listOf(applicationNumberList)
        } else {
            applicationNumberList.chunked((applicationNumberList.size + threadCount - 1) / threadCount)
        }
        val threadPool = Executors.newFixedThreadPool(
            chunks.size.coerceAtMost(threadCount)
        ).asCoroutineDispatcher()

        return try {
            runBlocking {
                chunks.map { chunk ->
                    async(threadPool) {
                        val httpClient = httpClientConfig.createHttpClient()
                        try {
                            val captcha =
                                tmCaptchaScraper.requestCaptcha(httpClient) // Get new CAPTCHA for each chunk/request
                            chunk.mapNotNull { number ->
                                scrapeTrademark(httpClient, number, captcha)
                            }
                        } catch (e: IllegalStateException) {
                            throw e
                        } finally {
                            httpClient.close()
                        }
                    }
                }.awaitAll().flatten().let {
                    tData.addAll(it)
                }
            }
            logger.info("All ${tData.size} trademarks scraped")
            tData // Return collected data only if everything succeeds
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error during scraping: ${e.message}", e)
            emptyList()
        } finally {
            threadPool.close()
        }
    }


}
