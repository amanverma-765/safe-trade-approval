package com.webxela.sta.backend.domain.model

data class OppositionReport(
    val reportId: Long? = null,
    val journalNumber: String,
    val ourAppId: String,
    val journalAppId: String,
    val report: String
)
