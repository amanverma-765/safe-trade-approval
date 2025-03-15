package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.LatestJournalMapper.toJournal
import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toTrademark
import com.webxela.sta.backend.domain.mapper.ReportMapping.toOppositionReport
import com.webxela.sta.backend.domain.mapper.ReportMapping.toOppositionReportEntity
import com.webxela.sta.backend.domain.model.OppositionReport
import com.webxela.sta.backend.domain.model.ReportGenRequest
import com.webxela.sta.backend.repo.JournalTmRepo
import com.webxela.sta.backend.repo.LatestJournalRepo
import com.webxela.sta.backend.repo.OppositionReportRepo
import com.webxela.sta.backend.repo.OurTrademarkRepo
import com.webxela.sta.backend.utils.extractTmPageFromPDF
import com.webxela.sta.backend.utils.generatePdfReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*


@Service
class OppositionService(
    private val oppositionReportRepo: OppositionReportRepo,
    private val latestJournalRepo: LatestJournalRepo,
    private val journalTmRepo: JournalTmRepo,
    private val ourTrademarkRepo: OurTrademarkRepo
) {

    private val logger = LoggerFactory.getLogger(OppositionService::class.java)

    suspend fun getGeneratedReports(): List<OppositionReport> = coroutineScope {
        oppositionReportRepo.findAll().map { it.toOppositionReport() }
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

    // Report generation
    suspend fun generateReport(requestData: ReportGenRequest): List<ByteArray> {
        val reportList = mutableListOf<ByteArray>()

        try {
            val journal = withContext(Dispatchers.IO) {
                latestJournalRepo.findByJournalNumber(requestData.journalNumber)?.toJournal()
            } ?: throw Exception("Invalid journal number")

            val journalTm = withContext(Dispatchers.IO) {
                journalTmRepo.findByApplicationNumber(
                    journalNumber = requestData.journalNumber,
                    applicationNumber = requestData.journalAppId
                ).getOrNull(0)
            }

            val isDeviceTrademark = journalTm?.tmType == "DEVICE"
            var imagePath: String? = null
            if (isDeviceTrademark) {
                val potentialImagePath =
                    "${System.getProperty("user.home")}/sta/staFiles/device/${journalTm?.applicationNumber}_device.jpg"
                if (File(potentialImagePath).exists()) {
                    imagePath = potentialImagePath
                } else {
                    logger.warn("Device trademark image not found at $potentialImagePath, using text fallback")
                }
            }

            val commonReplacements = mapOf(
                "{journalPublishDate}" to (journal.dateOfPublication),
                "{journalNumber}" to (requestData.journalNumber),
                "{journalTmName}" to (if (isDeviceTrademark && imagePath != null) "" else journalTm?.tmAppliedFor
                    ?: "NA"),
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

                val report = generatePdfReport(
                    templatePath = templatePath,
                    replacements = replacements,
                    imageReplacements = if (isDeviceTrademark && imagePath != null) {
                        mapOf("{journalTmName}" to imagePath)
                    } else {
                        emptyMap()
                    }
                )
                val reportWithOpposition =
                    addOppositionProof(report, requestData.journalNumber, journalTm?.applicationNumber!!)

                withContext(Dispatchers.IO) {
                    File(outputPath).writeBytes(reportWithOpposition)
                }

                reportList.add(reportWithOpposition)
                val oppositionReport = OppositionReport(
                    journalNumber = requestData.journalNumber,
                    ourAppId = ourAppId,
                    journalAppId = requestData.journalAppId,
                    report = fileName
                )
                oppositionReportRepo.save(oppositionReport.toOppositionReportEntity())
            }
            return reportList
        } catch (e: Exception) {
            logger.error("Error while generating report: ", e)
            throw e
        }
    }

    suspend fun addOppositionProof(
        report: ByteArray,
        journalNumber: String,
        applicationNumber: String
    ): ByteArray {
        val journalPath = System.getProperty("user.home") + "/sta/staFiles/$journalNumber"
        val directory = File(journalPath)

        if (!directory.exists() || !directory.isDirectory) {
            logger.error("Journal directory does not exist: $journalPath")
            throw IllegalArgumentException("Journal directory not found")
        }

        // Get all PDF files from the journal directory
        val pdfFiles = withContext(Dispatchers.IO) {
            directory.listFiles { file -> file.isFile && file.name.lowercase().endsWith(".pdf") }
                ?.map { it.absolutePath } ?: emptyList()
        }

        if (pdfFiles.isEmpty()) {
            logger.warn("No PDF files found in journal directory: $journalPath")
            return report
        }

        // Extract the trademark page from the PDFs
        val trademarkPage = extractTmPageFromPDF(pdfFiles, applicationNumber)
            ?: return report

        // Merge the report with the trademark page
        return withContext(Dispatchers.IO) {
            val outputStream = ByteArrayOutputStream()

            val reportTempFile = File.createTempFile("report", ".pdf")
            val trademarkTempFile = File.createTempFile("trademark", ".pdf")

            try {
                // Write bytes to temporary files
                Files.write(reportTempFile.toPath(), report)
                Files.write(trademarkTempFile.toPath(), trademarkPage)

                val merger = org.apache.pdfbox.multipdf.PDFMergerUtility()
                merger.destinationStream = outputStream

                merger.addSource(reportTempFile)
                merger.addSource(trademarkTempFile)
                merger.mergeDocuments(null)

                return@withContext outputStream.toByteArray()
            } finally {
                reportTempFile.delete()
                trademarkTempFile.delete()
            }
        }
    }


}