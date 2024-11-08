package com.webxela.sta.backend.domain.model


data class LatestJournal(
    val journalId: Long? = null,
    val journalNumber: String,
    val dateOfPublication: String,
    val dateOfAvailability: String,
    val filePath: String? = null,
    val fileName: String? = null
)
