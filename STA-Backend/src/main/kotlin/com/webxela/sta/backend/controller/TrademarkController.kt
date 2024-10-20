package com.webxela.sta.backend.controller

import com.webxela.sta.backend.domain.model.ErrorResponse
import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.domain.model.MatchingTrademark
import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.services.ScheduledTaskService
import com.webxela.sta.backend.services.TrademarkMatchingService
import com.webxela.sta.backend.services.TrademarkService
import org.springframework.http.HttpStatus
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
    suspend fun getLatestJournals(): ResponseEntity<List<Any>> {
        try {
            val journals = trademarkService.getLatestJournals()
            return ResponseEntity.ok(journals)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }

    @GetMapping("/get/our_trademarks")
    suspend fun getOurTrademarks(): ResponseEntity<List<Any>> {
        try {
            val trademarks = trademarkService.getOurTrademarks()
            return ResponseEntity.ok(trademarks)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }

    @GetMapping("/scrape/journal/application/{applicationId}")
    suspend fun scrapeJournalTmByApplicationId(
        @PathVariable applicationId: Long,
    ): ResponseEntity<Any> {
        try {
            val trademark = trademarkService.scrapeTrademark(applicationId.toString(), false)
            return ResponseEntity.ok(trademark)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    @GetMapping("/scrape/our/application/{applicationId}")
    suspend fun scrapeOurTmByApplicationId(
        @PathVariable applicationId: Long
    ): ResponseEntity<Any> {
        try {
            val trademark = trademarkService.scrapeTrademark(applicationId.toString(), true)
            return ResponseEntity.ok(trademark)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    @GetMapping("/match_trademarks/{journalNumbers}")
    suspend fun matchTheTrademark(
        @PathVariable journalNumbers: String
    ): ResponseEntity<List<Any>> {
        try {
            val journalList = journalNumbers.split("&")
            val matchingResult = trademarkMatchingService.findMatchingTrademarks(journalList)
            return ResponseEntity.ok(matchingResult)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }

    @GetMapping("/get/matching_result/{journalNumbers}")
    suspend fun getMatchingResult(
        @PathVariable journalNumbers: String
    ): ResponseEntity<List<Any>> {
        try {
            val journalList = journalNumbers.split("&")
            val matchingResult = trademarkMatchingService.getMatchingResult(journalList)
            return ResponseEntity.ok(matchingResult)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }

    @GetMapping("/start_schedule_task")
    suspend fun startScheduleTask(): ResponseEntity<Any> {
        try {
            scheduledTaskService.runTaskManually()
            return ResponseEntity.ok("Schedule task finished successfully")
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    @GetMapping("/generate_report")
    suspend fun generateReport(): ResponseEntity<Any> {
        try {
            scheduledTaskService.runTaskManually()
            return ResponseEntity.ok("Schedule task finished successfully")
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

}
