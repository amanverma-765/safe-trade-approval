package com.webxela.sta.backend.controller

import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.domain.model.MatchingTrademark
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.services.ScheduledTaskService
import com.webxela.sta.backend.services.TrademarkMatchingService
import com.webxela.sta.backend.services.TrademarkService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sta")
@CrossOrigin(origins = ["http://localhost:3000"])
class TrademarkController(
    private val trademarkService: TrademarkService,
    private val trademarkMatchingService: TrademarkMatchingService,
    private val scheduledTaskService: ScheduledTaskService
) {

    @GetMapping("/get/latest_journals")
    suspend fun getLatestJournals(): ResponseEntity<List<LatestJournal>> {
        val journals = trademarkService.getLatestJournals()
        return ResponseEntity.ok(journals)
    }

    @GetMapping("/get/our_trademarks")
    suspend fun getOurTrademarks(): ResponseEntity<List<Trademark>> {
        val trademarks = trademarkService.getOurTrademarks()
        return ResponseEntity.ok(trademarks)
    }

    @GetMapping("/scrape/journal/application/{applicationId}/{journalNumber}")
    suspend fun scrapeJournalTmByApplicationId(
        @PathVariable applicationId: String,
        @PathVariable journalNumber: String
    ): ResponseEntity<Trademark> {
        val trademark = trademarkService.scrapeJournalTmByApplicationId(journalNumber, applicationId)
        return ResponseEntity.ok(trademark)
    }

    @GetMapping("/scrape/our/application/{applicationId}")
    suspend fun scrapeOurTmByApplicationId(
        @PathVariable applicationId: Long
    ): ResponseEntity<Trademark> {
        val trademark = trademarkService.scrapeOurTmByApplicationId(applicationId.toString())
        return ResponseEntity.ok(trademark)
    }

    @GetMapping("/matchTrademarks/{journalNumbers}")
    suspend fun matchTheTrademark(
        @PathVariable journalNumbers: String
    ): ResponseEntity<List<MatchingTrademark>> {
        val journalList = journalNumbers.split("&")
        val matchingResult = trademarkMatchingService.findMatchingTrademarks(journalList)
        return ResponseEntity.ok(matchingResult)
    }

    @GetMapping("/get/matchingResult/{journalNumbers}")
    suspend fun getMatchingResult(
        @PathVariable journalNumbers: String
    ): ResponseEntity<List<MatchingTrademark>> {
        val journalList = journalNumbers.split("&")
        val matchingResult = trademarkMatchingService.getMatchingResult(journalList)
        return ResponseEntity.ok(matchingResult)
    }

    @GetMapping("/startScheduleTask")
    suspend fun startScheduleTask(): ResponseEntity<String> {
        scheduledTaskService.runTaskManually()
        return ResponseEntity.ok("Schedule task finished successfully")
    }

//    @GetMapping("/startScheduleTask")
//    suspend fun generateReport(): ResponseEntity<String> {
//        scheduledTaskService.runTaskManually()
//        return ResponseEntity.ok("Schedule task finished successfully")
//    }

}
