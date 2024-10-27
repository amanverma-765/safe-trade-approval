package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.LatestJournalMapper.toJournalEntity
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.repo.JournalTmRepo
import com.webxela.sta.backend.repo.LatestJournalRepo
import com.webxela.sta.backend.scraper.LatestJournalScraper
import com.webxela.sta.backend.scraper.StaScraper
import com.webxela.sta.backend.utils.extractNumbersFromPDF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service



@Service
class ScheduledTaskService(
    private val latestJournalScraper: LatestJournalScraper,
    private val latestJournalRepo: LatestJournalRepo,
    private val staScraper: StaScraper,
    private val dynamicJournalTmRepo: JournalTmRepo
) {

    private val logger = LoggerFactory.getLogger(ScheduledTaskService::class.java)

    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Kolkata")
    fun scheduleLatestJournalScraping() {
        runBlocking {
            var retryCount = 0
            val maxRetries = 4
            var success = false

            while (retryCount < maxRetries && !success) {
                try {
                    logger.info("Scheduled task attempt ${retryCount + 1} is started")
                    val journals = latestJournalScraper.fetchJournal()

                    val lastJournalNumber = withContext(Dispatchers.IO) {
                        latestJournalRepo.findLastJournalNumber()
                    }

                    if (lastJournalNumber == journals.last().journalNumber) {
                        logger.info("Journal Already Exists...")
                        return@runBlocking
                    }

                    val journalData: MutableList<Trademark> = mutableListOf()
                    val tableName = "journal_${journals.first().journalNumber}"

                    val savedFilePathList = journals.map { journal ->
                        System.getProperty("user.home") + "/sta/staFiles/${journal.journalNumber}/${journal.journalNumber}-${journal.fileName!!.replace(" ", "")}"
                    }
                    val applicationNumberList = extractNumbersFromPDF(savedFilePathList)
                    journalData.addAll(staScraper.scrapeTrademarkByList(applicationNumberList = applicationNumberList))
                    dynamicJournalTmRepo.addAll(tableName, journalData)
                    latestJournalRepo.save(journals.first().toJournalEntity())

                    logger.info("Scheduled task attempt ${retryCount + 1} is finished successfully")
                    success = true

                } catch (ex: Exception) {
                    retryCount++
                    logger.error("Error during scheduled scraping on attempt ${retryCount}: ", ex)

                    if (retryCount == maxRetries) {
                        logger.error("Max retry attempts reached. Task failed.")
                        throw RuntimeException("Scheduled task failed after $maxRetries attempts.")
                    } else {
                        logger.info("Retrying... ($retryCount/$maxRetries)")
                    }
                }
            }
        }
    }

    fun runTaskManually() {
        scheduleLatestJournalScraping()
    }
}

