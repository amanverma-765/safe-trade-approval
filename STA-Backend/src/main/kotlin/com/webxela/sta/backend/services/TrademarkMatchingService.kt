package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.model.MatchingTrademark
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

            val allOurTrademark = ourTrademarkRepo.findAll()
            if (allOurTrademark.isEmpty()) {
                throw NoSuchElementException("Our trademark is empty")
            }

            journalNumbers.forEach { journalNumber ->
                val journalTableName = "journal_$journalNumber"
                val journalTrademarks = journalTmRepo.findAll(journalTableName)
                val allMatchingTrademarks = mutableListOf<MatchingTrademark>()

                journalTrademarks.forEach { journalTrademark ->
                    val tmWordList = splitTrademarkNames(journalTrademark.tmAppliedFor)
                    val ourMatchedTmNumbers = mutableSetOf<String>()
                    tmWordList.forEach { tmWord ->
                        val ourMatchingTrademarks = ourTrademarkRepo.findByTmClassAndTmAppliedForContainingIgnoreCase(
                            tmClass = journalTrademark.tmClass,
                            tmAppliedFor = tmWord
                        )
                        if (ourMatchingTrademarks.isNotEmpty()) {
                            ourMatchedTmNumbers.addAll(ourMatchingTrademarks.map { it.applicationNumber })
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
            if (allOurTrademark.isNotEmpty()) {
                return@withContext getMatchingResult(journalNumbers)
            } else {
                throw Error("No matching result was found")
            }
        } catch (ex: Exception) {
            logger.error("Error while matching the table", ex)
            throw ex
        }
    }


    private fun splitTrademarkNames(trademarkName: String): List<String> {
        val tmWords = trademarkName.lowercase().split(" ")
        val cleanedTrademarks = mutableListOf<String>()
        tmWords.forEach { tm ->
            val cleanedTm = tm.replace("(", " ")
                .replace(")", " ")
                .replace(".", " ")
                .replace("\"", " ")
                .replace(",", " ")
                .replace("'", " ")
                .trim()
            cleanedTrademarks.add(cleanedTm)
        }
        val filteredCleanTmWords = cleanedTrademarks
            .filter { it !in stopWords }
            .filter { it.length > 3 }
        return filteredCleanTmWords
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
                data.ifEmpty {
                    throw NoSuchElementException("No Matching Result Available")
                }
                matchedTrademarks.addAll(data)
            }
        } catch (ex: Exception) {
            logger.error("Error while getting matching report from DB")
            throw ex
        }
        return@coroutineScope matchedTrademarks
    }


    val stopWords = listOf(
        "for", "the", "in", "and",
        "of", "to", "device", "the",
        "logo", "a", "as", "label",
        "with", "company", "trading",
        "utc", "limited", "devices",
        "by", "in", "/"
    )
}

