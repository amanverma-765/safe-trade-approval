package com.webxela.sta.backend.scraper.parser

import com.webxela.sta.backend.domain.model.Trademark
import com.webxela.sta.backend.utils.encodeAppNumber
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File


@Component
class TrademarkParser {

    private val logger = LoggerFactory.getLogger(TrademarkParser::class.java)

    fun parseTrademarkDetails(response: String, httpClient: HttpClient): Trademark? {

        var trademark: Trademark? = null

        try {
            val tableData = mutableMapOf<String, String>()
            val doc = Jsoup.parse(response)

            val tmDataPanel = doc.select("#panelgetdetail")

            // Extract key-value pairs from the main table
            val rows = tmDataPanel.select("table[border='1'] tr")
            if (rows.isEmpty()) {
                logger.error("Invalid trademark found")
                throw IllegalArgumentException("Invalid trademark found")
            }
            for (row in rows) {
                val cells = row.select("td")
                if (cells.size == 2) {
                    val key = cells[0].text().trim()
                    val value = cells[1].html().trim()
                        .replace("\n", ", ")
                        .replace("<br>", " ")
                        .replace("&nbsp;", " ")
                    tableData[key] = value
                }
            }

            // Extract Status
            val statusElement = doc.select("td font:contains(Status)").first()?.nextElementSibling()
            val status = statusElement?.text()?.trim()
            tableData["Status"] = status ?: run {
                logger.error("Status not found")
                throw RuntimeException("Status not found for trademark: ${tableData["TM Application No."]}")
            }

            // Extract image URL
            if (tableData["Trade Mark Type"]?.lowercase().equals("device")) {
                val encodedAppNumber = encodeAppNumber(tableData["TM Application No."] ?: "")
                val appNumber = tableData["TM Application No."] ?: ""
                val imgUrl = "https://tmrsearch.ipindia.gov.in/eregister/imagedoc.aspx?ID=1&APPNUMBER=$encodedAppNumber"

                // Create directory structure in user home directory
                val userHome = System.getProperty("user.home")
                val outputDir = File("$userHome${File.separator}staFiles${File.separator}device")
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }

                val outputPath = outputDir.absolutePath + File.separator + "${appNumber}_device.jpg"
                val file = File(outputPath)

                runBlocking {
                    try {
                        val imgResp = httpClient.get(imgUrl).readBytes()
                        file.writeBytes(imgResp)
                        logger.info("Image downloaded successfully to: $outputPath")
                    } catch (e: Exception) {
                        logger.error("Failed to download image: ${e.message}")
                    }
                }
            }


            trademark = Trademark(
                applicationNumber = tableData["TM Application No."] ?: throw RuntimeException("No Application Number found"),
                status = tableData["Status"] ?: throw RuntimeException("No Status found"),
                tmClass = tableData["Class"] ?: throw RuntimeException("No Class Found"),
                dateOfApplication = tableData["Date of Application"] ?: throw RuntimeException("No Date of Application found"),
                appropriateOffice = tableData["Appropriate Office"],
                state = tableData["State"],
                country = tableData["Country"],
                filingMode = tableData["Filing Mode"],
                tmAppliedFor = tableData["TM Applied For"] ?: throw RuntimeException("No TM Applied For found"),
                tmCategory = tableData["TM Category"],
                tmType = tableData["Trade Mark Type"] ?: throw RuntimeException("No Trade Mark Type found"),
                userDetails = tableData["User Detail"],
                certDetail = tableData["Certificate Detail"],
                validUpTo = tableData["Valid upto/ Renewed upto"],
                proprietorName = tableData["Proprietor name"],
                proprietorAddress = tableData["Proprietor Address"],
                emailId = tableData["Email Id"],
                agentName = tableData["Attorney name"],
                agentAddress = tableData["Attorney Address"],
                publicationDetails = tableData["Publication Details"]
            )

        } catch (ex: RuntimeException) {
            logger.error("Error while parsing trademark table data: ${ex.message}")
        }

        return trademark
    }

    fun checkIfOnRightPage(response: String): Boolean {
        var correctPage = false
        try {
            val doc = Jsoup.parse(response)
            val headers = doc.select("#SearchWMDatagrid tr:first-child td")
            for (header in headers) {
                if (header.text().contains("Proprietor Name", ignoreCase = true)) {
                    correctPage = true
                }
            }
        } catch (ex: RuntimeException) {
            correctPage = false
        }
        return correctPage
    }

}