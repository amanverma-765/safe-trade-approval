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

            val tables = doc.select("#panelgetdetail table")

            val targetTable = if (tables.size > 2 && tables[2].select("tr").size > 4) {
                tables[2]
            } else {
                tables.getOrNull(3) ?: throw Exception("Target table not found, Invalid response")
            }

            for (row in targetTable.select("tr")) {
                val cells = row.select("td")
                if (cells.size == 2) {
                    val key = cells[0].text().trim().lowercase().replace(" ", "")
                    val value = cells[1].text().trim()
                    tableData[key] = value
                }
            }

            try {
                val statusTable = tables[1]
                val statusRow = statusTable.select("tr")[1]
                val status = statusRow.selectFirst("font[color=red] b")?.text()
                status?.let {
                    tableData["status"] = it
                }
            } catch (ex: Exception) {
                logger.error("Error while getting trademark status for: ${tableData["tmapplicationno."]}")
            }


            trademark = Trademark(
                status = tableData["status"] ?: "Nil",
                applicationNumber = tableData["tmapplicationno."] ?: throw Exception("Application Number missing"),
                tmClass = tableData["class"] ?: throw Exception("Class missing"),
                dateOfApplication = tableData["dateofapplication"],
                appropriateOffice = tableData["appropriateoffice"],
                state = tableData["state"],
                country = tableData["country"],
                filingMode = tableData["filingmode"],
                tmAppliedFor = tableData["tmappliedfor"] ?: throw Exception("TM Applied For missing"),
                tmCategory = tableData["tmcategory"],
                tmType = tableData["trademarktype"],
                userDetails = tableData["userdetail"],
                certDetail = tableData["certificatedetail"],
                validUpTo = tableData["validupto/renewedupto"],
                proprietorName = tableData["proprietorname"],
                proprietorAddress = tableData["proprietoraddress"],
                emailId = tableData["emailid"],
                agentName = tableData["agentname"],
                agentAddress = tableData["agentaddress"],
                publicationDetails = tableData["publicationdetails"]
            )
        } catch (ex: Exception) {
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
        } catch (ex: Exception) {
            correctPage = false
        }
        return correctPage
    }

}