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
}
