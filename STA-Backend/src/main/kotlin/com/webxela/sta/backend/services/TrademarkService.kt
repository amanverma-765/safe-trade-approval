package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.LatestJournalMapper.toJournal
import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toOurTrademarkEntity
import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toTrademark
import com.webxela.sta.backend.domain.mapper.ReportMapping.toOppositionReport
import com.webxela.sta.backend.domain.mapper.ReportMapping.toOppositionReportEntity
import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.domain.model.OppositionReport
import com.webxela.sta.backend.domain.model.ReportGenRequest
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.repo.JournalTmRepo
import com.webxela.sta.backend.repo.LatestJournalRepo
import com.webxela.sta.backend.repo.OppositionReportRepo
import com.webxela.sta.backend.repo.OurTrademarkRepo
import com.webxela.sta.backend.scraper.StaScraper
import com.webxela.sta.backend.utils.extractNumbersFromExcel
import com.webxela.sta.backend.utils.generatePdfReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

@Service
class TrademarkService(
    private val latestJournalRepo: LatestJournalRepo,
    private val ourTrademarkRepo: OurTrademarkRepo,
    private val staScraper: StaScraper,
    private val journalTmRepo: JournalTmRepo
) {

    private val logger = LoggerFactory.getLogger(TrademarkService::class.java)

    suspend fun scrapeTrademark(
        appId: String,
        isOurTrademark: Boolean = false,
        journalNumber: String? = null
    ): Trademark? {
        try {
            require(appId.length <= 8) {
                logger.error("Application number $appId is too long")
                "Application Id is too long"
            }
            return withContext(Dispatchers.IO) {
                val trademark = when {
                    isOurTrademark -> {
                        // Fetch from our trademark repo if it's our trademark
                        ourTrademarkRepo.findByApplicationNumber(appId)?.toTrademark()
                    }

                    else -> {
                        // Fetch from journalTmRepo only if it is not our trademark
                        journalNumber?.let {
                            journalTmRepo.findByApplicationNumber(it, appId).getOrNull(0)
                        }
                    }
                } ?: run {
                    // If no trademark found, scrape it only if it's our trademark
                    if (isOurTrademark) {
                        val scrapedData = staScraper.scrapeByAppId(appId)
                            ?: throw NoSuchElementException("No trademark found for appId: $appId after scraping")
                        ourTrademarkRepo.save(scrapedData.toOurTrademarkEntity())
                        scrapedData
                    } else {
                        // Return null if it's not our trademark and nothing was found in the database
                        null
                    }
                }
                trademark
            }
        } catch (ex: Exception) {
            logger.error("Error while trademark scraping", ex)
            throw ex
        }
    }


    suspend fun scrapeOurTmByExcel(excelFile: FilePart) = coroutineScope {
        try {

            val tmNumberList = extractNumbersFromExcel(excelFile)
            val existingAppNumbers = withContext(Dispatchers.IO) {
                ourTrademarkRepo.findAllApplicationNumbers()
            }.toSet()

            val newTmNumbers = tmNumberList.filterNot { it in existingAppNumbers }.toSet()

            if (newTmNumbers.isEmpty()) {
                logger.info("No new trademarks to process. All trademarks already exist in the database.")
                return@coroutineScope
            }

            logger.info("Starting to scrape ${newTmNumbers.size} new trademarks")
            val trademarks = staScraper.scrapeTrademarkByList(newTmNumbers.toList())

            try {
                ourTrademarkRepo.saveAll(trademarks.map { it.toOurTrademarkEntity() })
            } catch (ex: DataIntegrityViolationException) {
                logger.warn("Duplicate entry ignored", ex)
            } catch (ex: Exception) {
                logger.error("Unexpected error while saving trademark", ex)
            }

            logger.info("Successfully processed ${trademarks.size} new trademarks")
        } catch (ex: Exception) {
            logger.error("Error processing trademarks from Excel: ", ex)
            throw ex
        }
    }

    suspend fun getOurTrademarks(): List<Trademark> = coroutineScope {
        ourTrademarkRepo.findAll().map { it.toTrademark() }
    }

    suspend fun deleteOurTrademark(appid: String) = coroutineScope {
        ourTrademarkRepo.deleteByApplicationNumber(appid)
    }

    suspend fun getLatestJournals(): List<LatestJournal> = coroutineScope {
        latestJournalRepo.findAll().map { it.toJournal() }
    }

}
