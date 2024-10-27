package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toTrademark
import com.webxela.sta.backend.domain.model.MatchingTrademark
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.repo.JournalTmRepo
import com.webxela.sta.backend.repo.MatchingTrademarkRepo
import com.webxela.sta.backend.repo.OurTrademarkRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TrademarkMatchingService(
    private val matchingTrademarkRepo: MatchingTrademarkRepo,
    private val ourTrademarkRepo: OurTrademarkRepo,
    private val journalTmRepo: JournalTmRepo
) {

    private val logger = LoggerFactory.getLogger(TrademarkMatchingService::class.java)

    suspend fun findMatchingTrademarks(journalNumbers: List<String>) = withContext(Dispatchers.IO) {
        try {
            val tmClassTrademarkCache = mutableMapOf<String, List<Trademark>>()

            journalNumbers.forEach { journalNumber ->
                val journalTableName = "journal_$journalNumber"
                val journalTrademarks = journalTmRepo.findAll(journalTableName)
                val allMatchingTrademarks = mutableListOf<MatchingTrademark>()

                journalTrademarks.forEach { journalTrademark ->
                    val ourTrademarks = tmClassTrademarkCache.getOrPut(journalTrademark.tmClass) {
                        ourTrademarkRepo.findByTmClass(journalTrademark.tmClass).map { it.toTrademark() }
                    }

                    val tmWordList = splitTrademarkNames(journalTrademark.tmAppliedFor)
                    val ourMatchedTmNumbers = mutableSetOf<String>()

                    tmWordList.forEach { tmWord ->
                        val matches = findSimilarTrademarksInMemory(tmWord, ourTrademarks)
                        if (matches.isNotEmpty()) {
                            ourMatchedTmNumbers.addAll(matches.map { it.applicationNumber })
                        }
                    }

                    if (ourMatchedTmNumbers.isNotEmpty()) {
                        val matchingTrademark = MatchingTrademark(
                            journalAppNumber = journalTrademark.applicationNumber,
                            ourTrademarkAppNumbers = ourMatchedTmNumbers.toList(),
                            tmClass = journalTrademark.tmClass,
                            journalNumber = journalNumber
                        )
                        allMatchingTrademarks.add(matchingTrademark)
                    }
                }

                if (allMatchingTrademarks.isNotEmpty()) {
                    saveAllMatchingTrademarks(
                        matchingTrademarks = allMatchingTrademarks,
                        journalNumber = journalNumber
                    )
                }
            }
            return@withContext getMatchingResult(journalNumbers)
        } catch (ex: Exception) {
            logger.error("Error while matching the table", ex)
            throw ex
        }
    }

    private fun splitTrademarkNames(trademarkName: String): List<String> {
        val tmWords = trademarkName.lowercase().split(" ")
        return tmWords
            .map { it.trim().replace("[().,\"']".toRegex(), " ") }
            .filter { it !in stopWords && it.length > 3 }
    }

    private fun findSimilarTrademarksInMemory(
        tmWord: String,
        ourTrademarks: List<Trademark>
    ): List<Trademark> {
        return ourTrademarks.filter {
            it.tmAppliedFor.contains(tmWord, ignoreCase = true)
        }
    }

    private fun saveAllMatchingTrademarks(
        matchingTrademarks: List<MatchingTrademark>,
        journalNumber: String
    ) {
        val matchingTableName = "matching_$journalNumber"
        matchingTrademarkRepo.replaceAll(tableName = matchingTableName, matchingTrademarks = matchingTrademarks)
    }

    suspend fun getMatchingResult(journalList: List<String>): List<MatchingTrademark> = coroutineScope {
        val matchedTrademarks = mutableListOf<MatchingTrademark>()
        try {
            journalList.forEach { journalNumber ->
                val tableName = "matching_$journalNumber"
                val data = matchingTrademarkRepo.findAll(tableName)
                if (data.isEmpty()) throw NoSuchElementException("No Matching Result Available")
                matchedTrademarks.addAll(data)
            }
        } catch (ex: Exception) {
            logger.error("Error while getting matching report from DB")
            throw ex
        }
        return@coroutineScope matchedTrademarks
    }

    private val stopWords = listOf(
        "for", "the", "in", "and", "of", "to", "device", "the",
        "logo", "a", "as", "label", "with", "company", "trading",
        "utc", "limited", "devices", "by", "in", "/"
    )
}
