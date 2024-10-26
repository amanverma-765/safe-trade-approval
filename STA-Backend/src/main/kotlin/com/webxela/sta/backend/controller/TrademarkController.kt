package com.webxela.sta.backend.controller

import com.webxela.sta.backend.domain.model.*
import com.webxela.sta.backend.services.ScheduledTaskService
import com.webxela.sta.backend.services.TrademarkMatchingService
import com.webxela.sta.backend.services.TrademarkService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sta")
@CrossOrigin(origins = ["http://localhost:3000", "http://52.172.161.167:3000"])
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

    @GetMapping("/delete/our/application/{applicationId}")
    suspend fun deleteOurTrademark(
        @PathVariable applicationId: Long,
    ): ResponseEntity<Any> {
        try {
            trademarkService.deleteOurTrademark(applicationId.toString())
            return ResponseEntity.ok("Successfully deleted $applicationId")
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    @PostMapping("/generate_report")
    suspend fun generateReport(
        @RequestBody reportGenRequest: ReportGenRequest
    ): ResponseEntity<List<Any>> {
        try {
            val reports = trademarkService.generateReport(reportGenRequest)
            return ResponseEntity.ok(reports)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }

    @GetMapping("/get/generated_reports")
    suspend fun getGeneratedReports(): ResponseEntity<List<Any>> {
        try {
            val reports = trademarkService.getGeneratedReports()
            return ResponseEntity.ok(reports)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }

    @GetMapping("/get/download_report/{reportId}")
    suspend fun downloadReport(
        @PathVariable reportId: Long
    ): ResponseEntity<Any> {
        try {
            val reportDoc = trademarkService.downloadReport(reportId)
            return ResponseEntity.ok(reportDoc)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    @GetMapping("/delete/report/{reportId}")
    suspend fun deleteReport(
        @PathVariable reportId: Long
    ): ResponseEntity<Any> {
        try {
            trademarkService.deleteReport(reportId)
            return ResponseEntity.ok("Report deleted successfully")
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

}
