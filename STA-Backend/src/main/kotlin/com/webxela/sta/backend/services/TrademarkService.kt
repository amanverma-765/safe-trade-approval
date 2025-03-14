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
    private val journalTmRepo: JournalTmRepo,
    private val oppositionReportRepo: OppositionReportRepo
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
                ourTrademarkRepo.findAllApplicationNumbers() // Assumes a custom query method to get only application numbers
            }.toSet()

            val newTmNumbers = tmNumberList.filterNot { it in existingAppNumbers }

            if (newTmNumbers.isEmpty()) {
                logger.info("No new trademarks to process. All trademarks already exist in the database.")
                return@coroutineScope
            }

            logger.info("Starting to scrape ${newTmNumbers.size} new trademarks")
            val trademarks = staScraper.scrapeTrademarkByList(newTmNumbers)

            withContext(Dispatchers.IO) {
                trademarks?.forEach { trademark ->
                    try {
                        ourTrademarkRepo.save(trademark.toOurTrademarkEntity())
                    } catch (ex: DataIntegrityViolationException) {
                        logger.warn("Duplicate entry ignored for application number: ${trademark.applicationNumber}", ex)
                    } catch (ex: Exception) {
                        logger.error("Unexpected error while saving trademark ${trademark.applicationNumber}", ex)
                    }
                }
            }

            logger.info("Successfully processed ${trademarks?.size} new trademarks")
        } catch (ex: Exception) {
            logger.error("Error processing trademarks from Excel: ", ex)
            throw ex
        }
    }


    suspend fun getLatestJournals(): List<LatestJournal> = coroutineScope {
        latestJournalRepo.findAll().map { it.toJournal() }
    }

    suspend fun getOurTrademarks(): List<Trademark> = coroutineScope {
        ourTrademarkRepo.findAll().map { it.toTrademark() }
    }

    suspend fun deleteOurTrademark(appid: String) = coroutineScope {
        ourTrademarkRepo.deleteByApplicationNumber(appid)
    }

    suspend fun getGeneratedReports(): List<OppositionReport> = coroutineScope {
        oppositionReportRepo.findAll().map { it.toOppositionReport() }
    }

    suspend fun generateReport(requestData: ReportGenRequest): List<ByteArray> {
        val reportList = mutableListOf<ByteArray>()

        val journal = withContext(Dispatchers.IO) {
            latestJournalRepo.findByJournalNumber(requestData.journalNumber)?.toJournal()
        } ?: throw Exception("Invalid journal number")

        val journalTm = withContext(Dispatchers.IO) {
            journalTmRepo.findByApplicationNumber(
                journalNumber = requestData.journalNumber,
                applicationNumber = requestData.journalAppId
            ).getOrNull(0)
        }

        val commonReplacements = mapOf(
            "{journalPublishDate}" to (journal.dateOfPublication),
            "{journalNumber}" to (requestData.journalNumber),
            "{journalTmName}" to (journalTm?.tmAppliedFor ?: "NA"),
            "{journalTmAppId}" to (journalTm?.applicationNumber ?: "NA"),
            "{journalDOA}" to (journalTm?.dateOfApplication ?: "NA"),
            "{journalClass}" to (journalTm?.tmClass ?: "NA"),
            "{journalDesc}" to (journalTm?.publicationDetails ?: "NA"),
            "{journalPr}" to (journalTm?.proprietorName ?: "NA"),
            "{journalUserDetail}" to (journalTm?.userDetails ?: "NA")
        )

        val templatePath = System.getProperty("user.home") + "/sta/staFiles/template.docx"
        val outputDir = System.getProperty("user.home") + "/sta/staFiles/reports"

        requestData.ourAppIdList.forEach { ourAppId ->
            val ourTm = withContext(Dispatchers.IO) {
                ourTrademarkRepo.findByApplicationNumber(applicationNumber = ourAppId)?.toTrademark()
            }

            val replacements = commonReplacements + mapOf(
                "{ourTmName}" to (ourTm?.tmAppliedFor ?: "NA"),
                "{ourAppId}" to (ourTm?.applicationNumber ?: "NA"),
                "{ourDOA}" to (ourTm?.dateOfApplication ?: "NA"),
                "{ourClass}" to (ourTm?.tmClass ?: "NA"),
                "{ourDesc}" to (ourTm?.publicationDetails ?: "NA"),
                "{ourPr}" to (ourTm?.proprietorName ?: "NA"),
                "{ourUserDetail}" to (ourTm?.userDetails ?: "NA")
            )

            val timestamp = SimpleDateFormat("HHmmssSSS").format(Date())
            val fileName = "opposition_${journalTm?.tmAppliedFor}-$ourAppId-$timestamp.pdf"
            val outputPath = "$outputDir/$fileName"
            File(outputDir).mkdirs()

            val report = generatePdfReport(templatePath, replacements, outputPath)
            reportList.add(report)
            val oppositionReport = OppositionReport(
                journalNumber = requestData.journalNumber,
                ourAppId = ourAppId,
                journalAppId = requestData.journalAppId,
                report = fileName
            )
            oppositionReportRepo.save(oppositionReport.toOppositionReportEntity())
        }
        return reportList
    }

    suspend fun downloadReport(reportId: Long): ByteArray {
        try {
            val fileName = withContext(Dispatchers.IO) {
                oppositionReportRepo.findById(reportId).orElseThrow()
            }.toOppositionReport().report

            val reportDir = System.getProperty("user.home") + "/sta/staFiles/reports"
            val finalPath = "$reportDir/$fileName"
            val file = Paths.get(finalPath).normalize().toAbsolutePath()
            return withContext(Dispatchers.IO) {
                Files.readAllBytes(file)
            }
        } catch (ex: Exception) {
            logger.error("Failed to download report", ex)
            throw IllegalAccessException("Failed to download report")
        }
    }

    suspend fun deleteReport(reportId: Long) = coroutineScope {
        try {
            val fileName = withContext(Dispatchers.IO) {
                oppositionReportRepo.findById(reportId).orElseThrow()
            }.toOppositionReport().report

            val reportDir = System.getProperty("user.home") + "/sta/staFiles/reports"
            val finalPath = "$reportDir/$fileName"
            withContext(Dispatchers.IO) {
                val filePath: Path = Paths.get(finalPath)
                if (Files.exists(filePath)) {
                    try {
                        Files.delete(filePath)
                    } catch (ex: Exception) {
                        logger.error("Failed to delete file at $finalPath. Error: ", ex)
                        throw Exception("Failed to delete report")
                    }
                } else {
                    logger.warn("File $fileName does not exist at $finalPath.")
                    throw Exception("File doesn't exist")
                }
            }
            withContext(Dispatchers.IO) {
                oppositionReportRepo.deleteById(reportId)
                logger.info("Report with ID $reportId deleted from repository.")
            }

        } catch (ex: Exception) {
            logger.error("Failed to delete file: ", ex)
            throw Exception("Failed to delete report")
        }
    }

}
