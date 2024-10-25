package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.LatestJournalMapper.toJournal
import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toOurTrademarkEntity
import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toTrademark
import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.domain.model.ReportGenRequest
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.repo.DynamicJournalTmRepo
import com.webxela.sta.backend.repo.LatestJournalRepo
import com.webxela.sta.backend.repo.OurTrademarkRepo
import com.webxela.sta.backend.scraper.StaScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.NoSuchElementException

@Service
class TrademarkService(
    private val latestJournalRepo: LatestJournalRepo,
    private val ourTrademarkRepo: OurTrademarkRepo,
    private val staScraper: StaScraper,
    private val dynamicJournalTmRepo: DynamicJournalTmRepo
) {

    private val logger = LoggerFactory.getLogger(TrademarkService::class.java)

    suspend fun scrapeTrademark(appId: String, isOurTrademark: Boolean): Trademark {
        require(appId.length <= 8) {
            logger.error("Application number $appId is too long")
            "Application Id is too long"
        }
        return withContext(Dispatchers.IO) {
            val trademark = (if (isOurTrademark) {
                ourTrademarkRepo.findByApplicationNumber(appId)?.toTrademark()
            } else {
                dynamicJournalTmRepo.findInAllTmEverywhere(appId)
            }) ?: run {
                val scrapedData = staScraper.scrapeByAppId(appId)
                    ?: throw NoSuchElementException("No trademark found for appId: $appId after scraping")
                if (isOurTrademark) {
                    ourTrademarkRepo.save(scrapedData.toOurTrademarkEntity())
                }
                scrapedData
            }
            trademark
        }
    }


    suspend fun getLatestJournals(): List<LatestJournal> = coroutineScope {
        latestJournalRepo.findAll().map { it.toJournal() }
    }

    suspend fun getOurTrademarks(): List<Trademark> = coroutineScope {
        ourTrademarkRepo.findAll().map { it.toTrademark() }
    }

    suspend fun deleteOurTrademark(appid: String) {
        withContext(Dispatchers.IO) {
            ourTrademarkRepo.deleteByApplicationNumber(appid)
        }
    }

    suspend fun generateReport(requestData: ReportGenRequest): List<ByteArray> {
        val reportList = mutableListOf<ByteArray>()

        val journal = withContext(Dispatchers.IO) {
            latestJournalRepo.findByJournalNumber(requestData.journalNumber).toJournal()
        }
        val journalTm = withContext(Dispatchers.IO) {
            dynamicJournalTmRepo.findByApplicationNumber(
                journalNumber = requestData.journalNumber,
                applicationNumber = requestData.journalAppId
            )
        }

        val commonReplacements = mapOf(
            "{journalPublishDate}" to (journal.dateOfPublication),
            "{journalNumber}" to (requestData.journalNumber),
            "{journalTmName}" to (journalTm?.tmAppliedFor ?: "NA"),
            "{journalTmAppId}" to (journalTm?.applicationNumber ?: "NA"),
            "{journalDOA}" to (journalTm?.dateOfApplication ?: "NA"),
            "{journalClass}" to (journalTm?.tmClass ?: "NA"),
            "{journalDesc}" to (journalTm?.publicationDetails ?: "NA"),
            "{journalPr}" to (journalTm?.proprietorName ?: "NA")
        )

        val templatePath = System.getProperty("user.home") + "/sta/staFiles/template.docx"
        val outputDir = System.getProperty("user.home") + "/sta/staFiles/reports"

        requestData.ourAppIdList.forEach { appId ->
            val ourTm = withContext(Dispatchers.IO) {
                ourTrademarkRepo.findByApplicationNumber(applicationNumber = appId)?.toTrademark()
            }

            val replacements = commonReplacements + mapOf(
                "{ourTmName}" to (ourTm?.tmAppliedFor ?: "NA"),
                "{ourAppId}" to (ourTm?.applicationNumber ?: "NA"),
                "{ourDOA}" to (ourTm?.dateOfApplication ?: "NA"),
                "{ourClass}" to (ourTm?.tmClass ?: "NA"),
                "{ourDesc}" to (ourTm?.publicationDetails ?: "NA"),
                "{ourPr}" to (ourTm?.proprietorName ?: "NA")
            )

            val timestamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
            val outputPath = "$outputDir/${requestData.journalAppId}-$appId-$timestamp.docx"
            File(outputDir).mkdirs()

            val report = generateDocxReport(templatePath, replacements, outputPath)
            reportList.add(report)
        }
        return reportList
    }

    private fun generateDocxReport(
        templatePath: String,
        replacements: Map<String, String>,
        outputPath: String
    ): ByteArray {
        FileInputStream(templatePath).use { inputStream ->
            XWPFDocument(inputStream).use { document ->
                // Process paragraphs for placeholder replacements
                document.paragraphs.forEach { paragraph ->
                    paragraph.runs.forEach { run ->
                        run.getText(0)?.takeIf { text -> replacements.keys.any { text.contains(it) } }?.let { text ->
                            var modifiedText = text
                            replacements.forEach { (placeholder, replacement) ->
                                modifiedText = modifiedText.replace(placeholder, replacement)
                            }
                            run.setText(modifiedText, 0)
                        }
                    }
                }

                // Process tables for placeholder replacements
                document.tables.forEach { table ->
                    table.rows.forEach { row ->
                        row.tableCells.forEach { cell ->
                            cell.paragraphs.forEach { paragraph ->
                                paragraph.runs.forEach { run ->
                                    run.getText(0)?.takeIf { text -> replacements.keys.any { text.contains(it) } }?.let { text ->
                                        var modifiedText = text
                                        replacements.forEach { (placeholder, replacement) ->
                                            modifiedText = modifiedText.replace(placeholder, replacement)
                                        }
                                        run.setText(modifiedText, 0)
                                    }
                                }
                            }
                        }
                    }
                }

                // Write the modified document to the specified output path
                FileOutputStream(outputPath).use { fileOutputStream ->
                    document.write(fileOutputStream)
                }
                // Return the document as a ByteArray
                ByteArrayOutputStream().use { outputStream ->
                    document.write(outputStream)
                    return outputStream.toByteArray()
                }
            }
        }
    }
}
