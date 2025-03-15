package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.LatestJournalMapper.toJournalEntity
import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.repo.JournalTmRepo
import com.webxela.sta.backend.repo.LatestJournalRepo
import com.webxela.sta.backend.scraper.LatestJournalScraper
import com.webxela.sta.backend.scraper.StaScraper
import com.webxela.sta.backend.utils.Constants.MAX_JOURNALS
import com.webxela.sta.backend.utils.extractNumbersFromPDF
import com.webxela.sta.backend.utils.retryWithExponentialBackoff
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths

@Service
class JournalScheduledTask(
    private val latestJournalScraper: LatestJournalScraper,
    private val latestJournalRepo: LatestJournalRepo,
    private val staScraper: StaScraper,
    private val journalTmRepo: JournalTmRepo
) {
    private val logger = LoggerFactory.getLogger(JournalScheduledTask::class.java)

    // Retry parameters
    private val maxRetries = 10 // Maximum number of retries
    private val initialRetryDelay = 120000L // Initial delay (2 min)
    private val maxRetryDelay = 600000L // Delay between retries (10 min)

    sealed class ScrapingResult {
        data object Success : ScrapingResult()
        data object JournalExists : ScrapingResult()
        data class Error(val exception: Exception) : ScrapingResult()
    }

    fun runJournalScrapingTaskManually() {
        scheduleLatestJournalScraping()
    }

    private suspend fun processJournalGroup(journalGroup: List<LatestJournal>): ScrapingResult {
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
            if (journalData.isEmpty()) throw RuntimeException("Failed to scrape journal: ${journalGroup.first().journalNumber}")

            // Save the data
            journalData.let { trademarks ->
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
                    logger.info("Journal with journalNumber $journalNumber already exists.")
                }
                runCleanup() // clean all extra unused journals
            }
            ScrapingResult.Success
        } catch (e: Exception) {
            ScrapingResult.Error(e)
        }
    }

    @Scheduled(cron = "0 0 0 * * WED", zone = "Asia/Kolkata")
    fun scheduleLatestJournalScraping() {
        logger.info("Starting scheduled journal scraping task")
        runBlocking {
            try {
                // Fetch journals
                val fetchedJournals = latestJournalScraper.fetchJournal()

                // Get existing journal numbers from the database
                val existingJournalNumbers = withContext(Dispatchers.IO) {
                    latestJournalRepo.findAll().map { it.journalNumber }.toSet()
                }

                // Filter out journals that already exist
                val journalsToProcess = fetchedJournals.filter {
                    !existingJournalNumbers.contains(it.journalNumber)
                }

                if (journalsToProcess.isEmpty()) {
                    logger.info("No new journals to process")
                    return@runBlocking
                }

                logger.info("Found ${journalsToProcess.map { it.journalNumber }.toSet()} new journals to process")

                // Process each new journal group sequentially, with independent retry mechanisms
                val groupedJournals = journalsToProcess.groupBy { it.journalNumber }.values.map { it.toList() }
                for (journalGroup in groupedJournals) {
                    val journalNumber = journalGroup.first().journalNumber
                    logger.info("Processing journal group $journalNumber")

                    try {
                        // Apply retry mechanism individually for each journal group
                        retryWithExponentialBackoff(maxRetries, initialRetryDelay, maxRetryDelay) {
                            when (val result = processJournalGroup(journalGroup)) {
                                is ScrapingResult.Success ->
                                    logger.info("Successfully processed journal $journalNumber")

                                is ScrapingResult.JournalExists ->
                                    logger.info("Journal $journalNumber already exists")

                                is ScrapingResult.Error ->
                                    throw result.exception
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to process journal $journalNumber after all retries", e)
                        // Continue with the next journal group instead of failing the entire process
                        continue
                    }
                }
            } catch (e: Exception) {
                logger.error("Fatal error in journal scraping task", e)
            }
        }
    }

    private fun runCleanup() {
        try {
            // Get all journals sorted by journal number in descending order (newest first)
            val allJournals = latestJournalRepo.findAll(Sort.by(Sort.Direction.DESC, "journalNumber"))

            if (allJournals.size > MAX_JOURNALS) {
                // Only delete the oldest journal if we exceed the limit
                val extraJournals = allJournals.takeLast(allJournals.size - MAX_JOURNALS)
                extraJournals.forEach { journalToDelete ->

                    latestJournalRepo.delete(journalToDelete)
                    logger.info("Cleaning up old journal: ${journalToDelete.journalNumber}")

                    // Delete the journal PDF from storage
                    val pdfPath = Paths.get(
                        System.getProperty("user.home"),
                        "sta",
                        "staFiles",
                        journalToDelete.journalNumber
                    )
                    try {
                        if (Files.exists(pdfPath)) {
                            Files.walk(pdfPath)
                                .sorted(Comparator.reverseOrder()) // Reverse order to delete contents before the directory
                                .forEach(Files::delete)
                            logger.info("Deleted folder: $pdfPath")
                        } else {
                            logger.info("Folder not found: $pdfPath")
                        }
                    } catch (ex: Exception) {
                        logger.error("Error deleting folder: $pdfPath", ex)
                    }

                    // Delete the journal table
                    try {
                        val tableName = "journal_${journalToDelete.journalNumber}"
                        journalTmRepo.deleteTable(tableName)
                        logger.info("Deleted table: $tableName")
                    } catch (e: Exception) {
                        logger.error("Error deleting table for journal ${journalToDelete.journalNumber}", e)
                    }
                }
            } else {
                logger.info("No cleanup needed, journal count (${allJournals.size}) does not exceed limit ($MAX_JOURNALS)")
            }
            logger.info("Finished cleanup")
        } catch (e: Exception) {
            logger.error("Fatal error in cleanup task", e)
        }
    }
}