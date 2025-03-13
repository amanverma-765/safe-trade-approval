package com.webxela.sta.backend.controller

import com.webxela.sta.backend.domain.model.ErrorResponse
import com.webxela.sta.backend.domain.model.JournalRequest
import com.webxela.sta.backend.domain.model.ReportGenRequest
import com.webxela.sta.backend.services.JournalScheduledTask
import com.webxela.sta.backend.services.OurTrademarkScheduledTask
import com.webxela.sta.backend.services.TrademarkMatchingService
import com.webxela.sta.backend.services.TrademarkService
import com.webxela.sta.backend.utils.isExcelFile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/sta")
class TrademarkController(
    private val trademarkService: TrademarkService,
    private val trademarkMatchingService: TrademarkMatchingService,
    private val journalScheduledTask: JournalScheduledTask,
    private val ourTrademarkScheduledTask: OurTrademarkScheduledTask
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

    @PostMapping("scrape/journal/application")
    suspend fun scrapeJournalTmByApplicationIds(
        @RequestBody request: JournalRequest
    ): ResponseEntity<Any?> {
        return try {
            val trademarks = request.applicationIds.mapNotNull { appId ->
                trademarkService.scrapeTrademark(
                    appId = appId,
                    journalNumber = request.journalNumber
                )
            }
            ResponseEntity.ok(trademarks)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }

    @PostMapping("scrape/our/application")
    suspend fun scrapeOurTmByApplicationIds(
        @RequestBody applicationIds: List<String>
    ): ResponseEntity<List<Any?>> {
        return try {
            val trademarks = applicationIds.map { appId ->
                trademarkService.scrapeTrademark(appId, true)
            }
            ResponseEntity.ok(trademarks)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }


    @PostMapping("/scrape/our/excel")
    suspend fun scrapeOurTmByExcel(
        @RequestPart("excelFile")
        excelFile: FilePart
    ): ResponseEntity<Any> {
        return try {
            if (!isExcelFile(excelFile)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse("Invalid file type. Please upload an Excel file."))
            }
            trademarkService.scrapeOurTmByExcel(excelFile)
            ResponseEntity.ok("Excel trademark scraped successfully")
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }


    @GetMapping("/match_trademarks/{journalNumber}")
    suspend fun matchTheTrademark(
        @PathVariable journalNumber: String
    ): ResponseEntity<List<Any>> {
        try {
            val matchingResult = trademarkMatchingService.findMatchingTrademarks(journalNumber)
            return ResponseEntity.ok(matchingResult)
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
        }
    }

//    @GetMapping("/get/matching_result/{journalNumbers}")
//    suspend fun getMatchingResult(
//        @PathVariable journalNumbers: String
//    ): ResponseEntity<List<Any>> {
//        try {
//            val journalList = journalNumbers.split("&")
//            val matchingResult = trademarkMatchingService.getMatchingResult(journalList)
//            return ResponseEntity.ok(matchingResult)
//        } catch (ex: Exception) {
//            val error = ErrorResponse(message = ex.message)
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(listOf(error))
//        }
//    }

    @GetMapping("/start_schedule_task/{task}")
    suspend fun startScheduleTask(@PathVariable task: String): ResponseEntity<Any> {
        try {
            when (task) {
                "journal" -> {
                    journalScheduledTask.runJournalScrapingTaskManually()
                    return ResponseEntity.ok("Latest journal scraped successfully")
                }
                "ourTm" -> {
                    ourTrademarkScheduledTask.runOurTrademarkStatusUpdateManually()
                    return ResponseEntity.ok("Status update for our trademarks completed successfully")
                }
                else -> return ResponseEntity.ok("Invalid task specified, please initiate a valid task")
            }
        } catch (ex: Exception) {
            val error = ErrorResponse(message = ex.message)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        }
    }

    @GetMapping("/delete/our/application/{applicationId}")
    suspend fun deleteOurTrademark(
        @PathVariable applicationId: String,
    ): ResponseEntity<Any> {
        try {
            trademarkService.deleteOurTrademark(applicationId)
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