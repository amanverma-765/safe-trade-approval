package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.model.MatchingTrademark
import com.webxela.sta.backend.repo.DynamicJournalTmRepo
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
    private val dynamicJournalTmRepo: DynamicJournalTmRepo
) {

    private val logger = LoggerFactory.getLogger(TrademarkMatchingService::class.java)

    suspend fun findMatchingTrademarks(journalNumbers: List<String>) = withContext(Dispatchers.IO) {
        try {
            journalNumbers.forEach { journalNumber ->
//                val matchingTableName = "matching_$journalNumber"

//                if (matchingTrademarkRepo.tableExists(matchingTableName)) {
//                    logger.info("Matching for journal $journalNumber is already done")
//                    return@forEach
//                }

                val journalTableName = "journal_$journalNumber"
                val journalTrademarks = dynamicJournalTmRepo.findAll(journalTableName)
                val allMatchingTrademarks = mutableListOf<MatchingTrademark>()

                journalTrademarks.forEach { journalTrademark ->
                    val ourTrademarks = ourTrademarkRepo.findByTmClass(journalTrademark.tmClass)

                    val similarTrademarks = ourTrademarks.filter {
                        isNameSimilar(journalTrademark.tmAppliedFor, it.tmAppliedFor)
                    }

                    if (similarTrademarks.isNotEmpty()) {
                        val matchingTrademark = MatchingTrademark(
                            journalAppNumber = journalTrademark.applicationNumber,
                            ourTrademarkAppNumbers = similarTrademarks.map { it.applicationNumber },
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

    private fun isNameSimilar(
        ourTm: String,
        journalTm: String,
    ): Boolean {

        val ourTmWords = cleanTm(ourTm.lowercase().split(" "))
        val journalTmWords = cleanTm(journalTm.lowercase().split(" "))

        // Remove stop words before comparison
        val filteredOurTmWords = ourTmWords.filter { it !in stopWords }
        val filteredJournalTmWords = journalTmWords.filter { it !in stopWords }

        // Ignore words shorter than 3 characters
        val significantOurTmWords = filteredOurTmWords.filter { it.length > 3 }
        val significantJournalTmWords = filteredJournalTmWords.filter { it.length > 3 }

        // Compare the remaining words
        for (existingWord in significantOurTmWords) {
            if (significantJournalTmWords.any {
                    it.contains(existingWord) || existingWord.contains(it)
                }
            ) {
                return true
            }
        }
        return false
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

    private fun cleanTm(trademarks: List<String>): List<String> {
        val cleanedTrademarks = mutableListOf<String>()
        trademarks.forEach { tm ->
            val cleanedTm = tm.replace("(", " ")
                .replace(")", " ")
                .replace(".", " ")
                .replace("\"", " ")
                .replace(",", " ")
                .trim()
            cleanedTrademarks.add(cleanedTm)
        }
        return cleanedTrademarks
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
