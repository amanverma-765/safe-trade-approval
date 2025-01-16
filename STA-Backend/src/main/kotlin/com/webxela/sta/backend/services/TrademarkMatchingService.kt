package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.OurTrademarkMapper.toTrademark
import com.webxela.sta.backend.domain.model.MatchingTrademark
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.repo.JournalTmRepo
import com.webxela.sta.backend.repo.OurTrademarkRepo
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TrademarkMatchingService(
    private val ourTrademarkRepo: OurTrademarkRepo,
    private val journalTmRepo: JournalTmRepo
) {
    private val logger = LoggerFactory.getLogger(TrademarkMatchingService::class.java)

    private val stopWords = listOf(
        "for", "the", "in", "and", "of", "to", "device", "the",
        "logo", "a", "as", "label", "with", "company", "trading",
        "utc", "limited", "devices", "by", "in", "/", "of", "llc",
        "inc", "corporation", "corp", "ltd", "private", "public",
        "design", "mark", "trademark", "brand", "solutions",
        "solution", "hindi"
    )

    suspend fun findMatchingTrademarks(journalNumber: String): List<MatchingTrademark> = coroutineScope {
        try {
            val tmClassTrademarkCache = mutableMapOf<String, List<Trademark>>()

            // Collect matching trademarks from all journal numbers
            val matchingTrademarks = async(Dispatchers.Default) {
                processJournalNumber(journalNumber, tmClassTrademarkCache)
            }.await()

            return@coroutineScope matchingTrademarks
        } catch (ex: Exception) {
            logger.error("Error while matching the table", ex)
            throw ex
        }
    }

    private suspend fun processJournalNumber(
        journalNumber: String,
        tmClassTrademarkCache: MutableMap<String, List<Trademark>>
    ): List<MatchingTrademark> = withContext(Dispatchers.IO) {
        val journalTableName = "journal_$journalNumber"
        val journalTrademarks = journalTmRepo.findAll(journalTableName)
        val allMatchingTrademarks = mutableListOf<MatchingTrademark>()

        journalTrademarks.forEach { journalTrademark ->
            val matchingTrademark = processTrademark(journalTrademark, journalNumber, tmClassTrademarkCache)
            matchingTrademark?.let { allMatchingTrademarks.add(it) }
        }

        return@withContext allMatchingTrademarks
    }

    private suspend fun processTrademark(
        journalTrademark: Trademark,
        journalNumber: String,
        tmClassTrademarkCache: MutableMap<String, List<Trademark>>
    ): MatchingTrademark? = withContext(Dispatchers.Default) {
        val ourTrademarks = tmClassTrademarkCache.getOrPut(journalTrademark.tmClass) {
            ourTrademarkRepo.findByTmClass(journalTrademark.tmClass).map { it.toTrademark() }
        }

        // Perform trademark matching
        val matchResults = findMatchingTrademarks(
            journalTrademark.tmAppliedFor,
            ourTrademarks.map { it.tmAppliedFor }
        )

        if (matchResults.isNotEmpty()) {
            val ourMatchedTrademark = matchResults.map { match ->
                // Find the corresponding trademark from our list
                val matchedOurTrademark = ourTrademarks.first { it.tmAppliedFor == match.matchedTrademark }

                MatchingTrademark.OurTmDataMini(
                    tmApplicationNumber = matchedOurTrademark.applicationNumber,
                    tmAppliedFor = match.matchedTrademark,
                    tmClass = journalTrademark.tmClass,
                    matchedSubstrings = match.matchedSubstrings
                )
            }

            return@withContext MatchingTrademark(
                journalNumber = journalNumber,
                tmClass = journalTrademark.tmClass,
                journalTrademark = MatchingTrademark.JournalTmDataMini(
                    tmApplicationNumber = journalTrademark.applicationNumber,
                    tmAppliedFor = journalTrademark.tmAppliedFor,
                    tmClass = journalTrademark.tmClass
                ),
                ourTrademarks = ourMatchedTrademark
            )
        }

        return@withContext null
    }

    // Trademark matching algorithm
    private fun findMatchingTrademarks(
        journalTrademark: String,
        ourTrademarks: List<String>,
        minSubstringLength: Int = 4
    ): List<TrademarkMatch> {
        val matchResults = mutableListOf<TrademarkMatch>()

        val journalTokens = tokenize(journalTrademark)

        for (trademark in ourTrademarks) {
            val trademarkTokens = tokenize(trademark)

            val matchedSubstrings = mutableListOf<String>()

            for (journalToken in journalTokens) {
                for (trademarkToken in trademarkTokens) {
                    val commonSubstring = findCommonSubstring(journalToken, trademarkToken, minSubstringLength)
                    if (commonSubstring != null) {
                        matchedSubstrings.add(commonSubstring)
                    }
                }
            }

            if (matchedSubstrings.isNotEmpty()) {
                matchResults.add(
                    TrademarkMatch(
                        journalTrademark,
                        trademark,
                        matchedSubstrings.distinct()
                    )
                )
            }
        }

        return matchResults
    }

    private fun tokenize(trademark: String): List<String> {
        return trademark.lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .split("\\s+".toRegex())
            .filter { word ->
                word.isNotBlank()
                        && word !in stopWords
            }
    }

    private fun findCommonSubstring(
        token1: String,
        token2: String,
        minLength: Int
    ): String? {
        val str1 = token1.lowercase()
        val str2 = token2.lowercase()

        val shorterToken = if (str1.length <= str2.length) str1 else str2
        val longerToken = if (str1.length <= str2.length) str2 else str1

        for (length in shorterToken.length downTo minLength) {
            for (start in 0..shorterToken.length - length) {
                val substring = shorterToken.substring(start, start + length)
                if (longerToken.contains(substring)) {
                    return substring
                }
            }
        }

        return null
    }

    private data class TrademarkMatch(
        val journalTrademark: String,
        val matchedTrademark: String,
        val matchedSubstrings: List<String>
    )
}