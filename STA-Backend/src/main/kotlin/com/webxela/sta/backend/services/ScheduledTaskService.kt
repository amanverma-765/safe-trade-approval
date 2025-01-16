package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.LatestJournalMapper.toJournalEntity
import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.repo.JournalTmRepo
import com.webxela.sta.backend.repo.LatestJournalRepo
import com.webxela.sta.backend.scraper.LatestJournalScraper
import com.webxela.sta.backend.scraper.StaScraper
import com.webxela.sta.backend.utils.Constants.MAX_TRADEMARKS
import com.webxela.sta.backend.utils.extractNumbersFromPDF
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

@Service
class ScheduledTaskService(
    private val latestJournalScraper: LatestJournalScraper,
    private val latestJournalRepo: LatestJournalRepo,
    private val staScraper: StaScraper,
    private val journalTmRepo: JournalTmRepo
) {
    private val logger = LoggerFactory.getLogger(ScheduledTaskService::class.java)

    data class RetryConfig(
        val maxAttempts: Int = 10,
        val initialDelay: Long = 5,  // seconds
        val maxDelay: Long = 30,     // seconds
        val factor: Double = 2.0
    )

    sealed class ScrapingResult {
        data object Success : ScrapingResult()
        data object JournalExists : ScrapingResult()
        data class Error(val exception: Exception) : ScrapingResult()
    }

    private suspend fun processJournalGroup(
        journalGroup: List<LatestJournal>
    ): ScrapingResult {
        return try {
            val tableName = "journal_${journalGroup.first().journalNumber}"

            // Process the journal data
            val savedFilePathList = journalGroup.map { journal ->
                System.getProperty("user.home") + "/sta/staFiles/${journal.journalNumber}/${
                    journal.journalNumber
                }-${journal.fileName!!.replace(" ", "")}"
            }

            val applicationNumberList = extractNumbersFromPDF(savedFilePathList)
            val journalData = staScraper.scrapeTrademarkByList(applicationNumberList)

            // Save the data
            journalData?.let { trademarks ->
                withContext(Dispatchers.IO) {
                    journalTmRepo.replaceAll(tableName, trademarks)
                }
            }

            withContext(Dispatchers.IO) {
                val journalNumber = journalGroup.first().journalNumber
                val existingJournal = latestJournalRepo.findByJournalNumber(journalNumber)

                if (existingJournal == null) {
                    // Save the new journal
                    latestJournalRepo.save(journalGroup.first().toJournalEntity())
                } else {
                    println("Journal with journalNumber $journalNumber already exists.")
                }
                runCleanup() // clean all extra unused journals
            }
            ScrapingResult.Success
        } catch (e: Exception) {
            ScrapingResult.Error(e)
        }
    }

    private suspend fun <T> retryWithExponentialBackoff(
        config: RetryConfig = RetryConfig(),
        operation: suspend () -> T
    ): T {
        var currentDelay = config.initialDelay
        var attemptCount = 0

        while (true) {
            try {
                return operation()
            } catch (e: Exception) {
                attemptCount++
                if (attemptCount >= config.maxAttempts) {
                    logger.error("Final attempt $attemptCount failed after ${config.maxAttempts} retries", e)
                    throw RuntimeException("Operation failed after ${config.maxAttempts} attempts", e)
                }

                logger.warn("Attempt $attemptCount failed, retrying in $currentDelay seconds", e)
                delay(currentDelay.seconds)

                // Calculate next delay with exponential backoff
                currentDelay = (currentDelay * config.factor)
                    .toLong()
                    .coerceAtMost(config.maxDelay)
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * WED", zone = "Asia/Kolkata")
    fun scheduleLatestJournalScraping() {
        runBlocking {
            try {
                retryWithExponentialBackoff {
                    // Fetch journals
                    val journals = latestJournalScraper.fetchJournal()

                    // Process each journal group sequentially
                    val groupedJournals = journals.groupBy { it.journalNumber }.values.map { it.toList() }
                    for (journalGroup in groupedJournals) {
                        when (val result = processJournalGroup(journalGroup)) {
                            is ScrapingResult.Success ->
                                logger.info("Successfully processed journal ${journalGroup.first().journalNumber}")

                            is ScrapingResult.JournalExists ->
                                logger.info("Journal ${journalGroup.first().journalNumber} already exists")

                            is ScrapingResult.Error ->
                                throw result.exception
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Fatal error in journal scraping task", e)
                throw e
            }
        }
    }

    fun runTaskManually() {
        scheduleLatestJournalScraping()
    }

    private fun runCleanup() {
        try {
            // Delete Extra Journals from listing
            val allJournals = latestJournalRepo.findAll(Sort.by(Sort.Direction.DESC, "journalNumber"))
            if (allJournals.size > MAX_TRADEMARKS) {
                val journalsToDelete = allJournals.drop(MAX_TRADEMARKS) // Keep the top, delete the rest
                latestJournalRepo.deleteAll(journalsToDelete)

                val journalNumbersToDelete = journalsToDelete.map { it.journalNumber }

                // Delete Extra Journals PDF from storage
                journalNumbersToDelete.forEach { journalNumber ->
                    val pdfPath = Paths.get(System.getProperty("user.home"), "sta", "staFiles", journalNumber.toString())
                    try {
                        if (Files.exists(pdfPath)) {
                            Files.walk(pdfPath)
                                .sorted(Comparator.reverseOrder()) // Reverse order to delete contents before the directory
                                .forEach(Files::delete)
                            println("Deleted folder: $pdfPath")
                        } else {
                            println("Folder not found: $pdfPath")
                        }
                    } catch (ex: Exception) {
                        println("Error deleting folder: $pdfPath")
                        ex.printStackTrace()
                    }
                }

                // Delete Extra Journal Tables
                try {
                    journalNumbersToDelete.forEach { journalNumber ->
                        val tableName = "journal_$journalNumber"
                        journalTmRepo.deleteTable(tableName)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            logger.info("Finished cleanup...")
        } catch (e: Exception) {
            logger.error("Fatal error in cleanup task", e)
            e.printStackTrace()
        }
    }

}