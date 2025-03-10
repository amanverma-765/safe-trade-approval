package com.webxela.sta.backend.scraper

import com.webxela.sta.backend.config.KtorClientConfig
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.scraper.parser.TrademarkParser
import com.webxela.sta.backend.utils.Constants.CAPTCHA_URL
import com.webxela.sta.backend.utils.Constants.MAX_THREADS
import com.webxela.sta.backend.utils.Constants.TRADEMARK_URL
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
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
            val captcha = getCaptcha(httpClient)
            trademark = scrapeTrademark(httpClient = httpClient, appId = appId, captcha = captcha)
//            logger.error("877777777777fg================= is $trademark")
        } catch (ex: Exception) {
            logger.error("Error while scraping by application id ${ex.message}")
            throw ex
        }
        return trademark
    }

    // Get CAPTCHA
    private suspend fun getCaptcha(httpClient: HttpClient): String {
        logger.info("Fetching new CAPTCHA...")
        httpClient.get(CAPTCHA_URL)
        httpClient.get(TRADEMARK_URL)
        val captcha = tmCaptchaScraper.requestCaptcha(httpClient)
        return captcha!!
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
                tmData = trademarkParser.parseTrademarkDetails(response)
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
    ): List<Trademark>? {
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
                            val captcha = getCaptcha(httpClient)  // Get new CAPTCHA for each chunk/request
                            chunk.mapNotNull { number ->
                                scrapeTrademark(httpClient, number, captcha)
                            }
                        } finally {
                            httpClient.close()
                        }
                    }
                }.awaitAll().flatten().let {
                    tData.addAll(it)
                }
            }
            println("All ${tData.size} trademarks scraped")
            tData // Return collected data only if everything succeeds
        } catch (e: Exception) {
            println("Error during scraping: ${e.message}")
            null // Return null if any error occurs
        } finally {
            threadPool.close()
        }
    }


}
