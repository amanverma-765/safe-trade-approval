package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.LatestJournalMapper.toJournal
import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toOurTrademarkEntity
import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toTrademark
import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.repo.DynamicJournalTmRepo
import com.webxela.sta.backend.repo.LatestJournalRepo
import com.webxela.sta.backend.repo.OurTrademarkRepo
import com.webxela.sta.backend.scraper.StaScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TrademarkService(
    private val latestJournalRepo: LatestJournalRepo,
    private val ourTrademarkRepo: OurTrademarkRepo,
    private val staScraper: StaScraper,
    private val dynamicJournalTmRepo: DynamicJournalTmRepo
) {

    private val logger = LoggerFactory.getLogger(TrademarkService::class.java)

    private suspend fun fetchTrademarkData(appid: String, journalName: String? = null): Trademark {
        if (appid.length > 8) {
            logger.error("Application number is too long")
            throw NoSuchElementException("Application Id is too long")
        }

        return withContext(Dispatchers.IO) {
            if (journalName != null) {
                dynamicJournalTmRepo.findByApplicationNumber(journalName, appid)
                    ?: throw NoSuchElementException("No trademark found for appId: $appid in journal: $journalName")
            } else {
                val trademark = ourTrademarkRepo.findByApplicationNumber(appid)?.toTrademark()

                if (trademark != null) {
                    trademark
                } else {
                    val trademarkData = staScraper.scrapeByAppId(appId = appid)
                    if (trademarkData != null) {
                        ourTrademarkRepo.save(trademarkData.toOurTrademarkEntity())
                        trademarkData
                    } else {
                        throw NoSuchElementException("No trademark found for appId: $appid after scraping")
                    }
                }
            }
        }
    }


    suspend fun scrapeOurTmByApplicationId(appid: String): Trademark = coroutineScope {
        fetchTrademarkData(appid)
    }

    suspend fun scrapeJournalTmByApplicationId(
        journalNumber: String,
        appid: String
    ): Trademark = coroutineScope {
        val journalName = "journal_$journalNumber"
        fetchTrademarkData(appid, journalName)
    }

    suspend fun getLatestJournals(): List<LatestJournal> = coroutineScope {
        latestJournalRepo.findAll().map { it.toJournal() }
    }

    suspend fun getOurTrademarks(): List<Trademark> = coroutineScope {
        ourTrademarkRepo.findAll().map { it.toTrademark() }
    }
}
