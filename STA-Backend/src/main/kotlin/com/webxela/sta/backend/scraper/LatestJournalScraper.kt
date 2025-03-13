package com.webxela.sta.backend.scraper

import com.webxela.sta.backend.config.KtorClientConfig
import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.utils.Constants.JOURNAL_VIEW_URL
import com.webxela.sta.backend.utils.Constants.MAX_JOURNALS
import com.webxela.sta.backend.utils.Constants.TM_LISTING_PAGE
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

@Component
class LatestJournalScraper(private val httpClientConfig: KtorClientConfig) {

    private val logger = LoggerFactory.getLogger(LatestJournalScraper::class.java)

    suspend fun fetchJournal(): MutableList<LatestJournal> {

        val httpClient = httpClientConfig.createHttpClient()
        val journals = mutableListOf<LatestJournal>()

        try {
            // Fetch the TM listing page
            val response = httpClient.get(TM_LISTING_PAGE)
            val document = Jsoup.parse(response.bodyAsText().trimIndent())
            val allRows = document.select("table#Journal tbody tr").take(MAX_JOURNALS)
            // Extract data from the row
            allRows.forEach { row ->
                val cells = row.getElementsByTag("td")
                cells.let { rowCells ->
                    if (rowCells.size >= 5) {
                        val journalNumber = rowCells[1].text()
                        val dateOfPublication = rowCells[2].text()
                        val dateOfAvailability = rowCells[3].text()

                        // Extract the journal file paths
                        val journalPaths = row.select("form input[name=FileName]").map { it.attr("value") }

                        val regex = Regex("([^\\\\/]+\\.pdf)$")

                        journalPaths.forEach { path ->
                            val matchResult = regex.find(path)
                            val filename = matchResult?.groups?.get(1)?.value
                            filename?.let { name ->
                                // Add journal to the list
                                journals.add(
                                    LatestJournal(
                                        journalNumber = journalNumber,
                                        dateOfPublication = dateOfPublication,
                                        dateOfAvailability = dateOfAvailability,
                                        filePath = path,
                                        fileName = name
                                    )
                                )

                                // File path and directory management
                                val actualFilePath =
                                    System.getProperty("user.home") + "/sta/staFiles/$journalNumber/$journalNumber-${
                                        name.replace(
                                            " ",
                                            ""
                                        )
                                    }"
                                val tempDirectory =
                                    File(System.getProperty("user.home") + "/sta/staFiles/$journalNumber")

                                // Ensure the directory exists
                                if (!tempDirectory.exists()) {
                                    tempDirectory.mkdirs()
                                    logger.info("Directory ${tempDirectory.path} created")
                                }

                                // Check if file already exists
                                if (File(actualFilePath).exists()) {
                                    logger.info("File $filename already exists at path $actualFilePath, no need to download!")
                                } else {
                                    try {
                                        // Downloading the journal PDF
                                        logger.info("Downloading journal $name")
                                        val journalResponse = httpClient.get(JOURNAL_VIEW_URL) {
                                            setBody(
                                                MultiPartFormDataContent(
                                                    formData { append("FileName", path) }
                                                )
                                            )
                                            timeout {
                                                requestTimeoutMillis = 600000
                                                socketTimeoutMillis = 600000
                                            }
                                        }

                                        // Write the file
                                        withContext(Dispatchers.IO) {
                                            File(actualFilePath).writeBytes(journalResponse.readBytes())
                                        }

                                        // Retry mechanism to ensure the file is saved
                                        val maxRetries = 20
                                        val retryDelay = 1000L
                                        var isFileSaved = false

                                        for (i in 1..maxRetries) {
                                            if (File(actualFilePath).exists()) {
                                                isFileSaved = true
                                                logger.info("PDF downloaded and saved to $actualFilePath")
                                                break
                                            } else {
                                                logger.info("File not found yet. Retrying... attempt $i")
                                                delay(retryDelay)
                                            }
                                        }

                                        if (!isFileSaved) {
                                            logger.error("File could not be saved within the time limit.")
                                        }
                                    } catch (ex: Exception) {
                                        logger.error("Error while downloading journal $name: ${ex.message}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (ex: Exception) {
            logger.error("Error while fetching latest journal ${ex.message}")
            throw ex
        }
        return journals
    }
}



