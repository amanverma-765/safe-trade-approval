package com.webxela.sta.backend.domain.model

data class ReportGenRequest(
    val journalAppId: String,
    val ourAppIdList: List<String>,
)
