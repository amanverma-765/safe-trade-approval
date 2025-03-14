package com.webxela.sta.backend.scraper.parser

import com.webxela.sta.backend.domain.model.Trademark
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class TrademarkParser {

    private val logger = LoggerFactory.getLogger(TrademarkParser::class.java)

    fun parseTrademarkDetails(response: String): Trademark? {

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