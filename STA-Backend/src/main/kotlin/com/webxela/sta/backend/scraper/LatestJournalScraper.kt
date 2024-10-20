package com.webxela.sta.backend.scraper

import com.webxela.sta.backend.config.KtorClientConfig
import com.webxela.sta.backend.domain.mapper.LatestJournalMapper.toJournalEntity
import com.webxela.sta.backend.domain.model.LatestJournal
import com.webxela.sta.backend.repo.LatestJournalRepo
import com.webxela.sta.backend.utils.Constants.JOURNAL_VIEW_URL
import com.webxela.sta.backend.utils.Constants.TM_LISTING_PAGE
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File

@Component
class LatestJournalScraper(
    private val httpClientConfig: KtorClientConfig,
) {

    private val logger = LoggerFactory.getLogger(LatestJournalScraper::class.java)

    suspend fun fetchJournal(): MutableList<LatestJournal> {

        val httpClient = httpClientConfig.createHttpClient()
        val journals = mutableListOf<LatestJournal>()

        try {
            val response = httpClient.get(TM_LISTING_PAGE)
            val document = Jsoup.parse(response.bodyAsText().trimIndent())
            val row = document.select("table#Journal tbody tr").first()

            val cells = row?.getElementsByTag("td")

            cells?.let { rowCells ->
                if (rowCells.size >= 5) {
                    val journalNumber = rowCells[1].text()
                    val dateOfPublication = rowCells[2].text()
                    val dateOfAvailability = rowCells[3].text()

                    val journalPaths = row.select("form input[name=FileName]").map { it.attr("value") }

                    val regex = Regex("([^\\\\/]+\\.pdf)$")

                    journalPaths.forEach { path ->
                        val matchResult = regex.find(path)
                        val filename = matchResult?.groups?.get(1)?.value
                        filename?.let { name ->
                            journals.add(
                                LatestJournal(
                                    journalNumber = journalNumber,
                                    dateOfPublication = dateOfPublication,
                                    dateOfAvailability = dateOfAvailability,
                                    filePath = path,
                                    fileName = name
                                )
                            )

                            val actualFilePath = "temp/$journalNumber-${name.replace(" ", "")}"
                            if (File(actualFilePath).exists()) {
                                logger.info("File $filename already exists at path $actualFilePath no need to download !!")
                            } else {

                                // Downloading the journal pdf
                                logger.info("Downloading jounal $name")
                                val journalResponse = httpClient.get(JOURNAL_VIEW_URL) {
                                    setBody(
                                        MultiPartFormDataContent(
                                            formData { append("FileName", path) })
                                    )
                                    timeout {
                                        requestTimeoutMillis = 600000
                                        socketTimeoutMillis = 600000
                                    }
                                }
                                withContext(Dispatchers.IO) {
                                    File(actualFilePath).writeBytes(journalResponse.readBytes())
                                }
                                val maxRetries = 20 // Total number of retries
                                val retryDelay = 1000L // 1 second delay between retries
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