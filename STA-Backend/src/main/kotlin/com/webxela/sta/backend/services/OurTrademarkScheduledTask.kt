package com.webxela.sta.backend.services

import com.webxela.sta.backend.repo.OurTrademarkRepo
import com.webxela.sta.backend.scraper.StaScraper
import com.webxela.sta.backend.utils.retryWithExponentialBackoff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class OurTrademarkScheduledTask(
    private val ourTrademarkRepo: OurTrademarkRepo,
    private val staScraper: StaScraper
) {

    private val logger = LoggerFactory.getLogger(OurTrademarkScheduledTask::class.java)

    fun runOurTrademarkStatusUpdateManually() {
        scheduleOurTrademarkStatusUpdate()
    }

    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Kolkata")
    fun scheduleOurTrademarkStatusUpdate() {
        logger.info("Starting scheduled OurTrademark scraping task")
        try {
            runBlocking {
                try {
                    // Check if the repository is still accessible
                    val ourTmAppIds = try {
                        withContext(Dispatchers.IO) {
                            ourTrademarkRepo.findAllApplicationNumbers()
                        }
                    } catch (e: Exception) {
                        if (e.cause is IllegalStateException &&
                            e.cause?.message?.contains("has been closed already") == true
                        ) {
                            logger.warn("Application context is closing, skipping scheduled task")
                            return@runBlocking
                        }
                        throw e
                    }

                    // Scrape all the collected trademarks with retry mechanism
                    val ourScrapedTrademarks = retryWithExponentialBackoff {
                        val trademarks = staScraper.scrapeTrademarkByList(ourTmAppIds)
                        if (trademarks.isEmpty()) {
                            logger.warn("No trademarks scraped, retrying...")
                            throw RuntimeException("Empty trademark result, triggering retry")
                        }
                        trademarks
                    }

                    val tmStatusMap: Map<String, String> = ourScrapedTrademarks.associate { trademark ->
                        trademark.applicationNumber to (trademark.status ?: "NA")
                    }

                    // Update the status in database with the new trademarks
                    if (tmStatusMap.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            tmStatusMap.entries.forEach { entry ->
                                ourTrademarkRepo.updateTrademarkStatus(entry.key, entry.value)
                            }
                        }
                    }
                    logger.info("Trademark database successfully updated with ${ourScrapedTrademarks.size} entries")
                } catch (ex: Exception) {
                    logger.error("Failed to scrape and update trademarks", ex)
                }
            }
        } catch (e: Exception) {
            logger.error("Critical error in scheduled task", e)
        }
    }
}