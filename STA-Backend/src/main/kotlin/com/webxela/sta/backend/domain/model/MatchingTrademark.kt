package com.webxela.sta.backend.domain.model

data class MatchingTrademark(
    val matchResultId: Long? = null,
    val journalAppNumber: String,
    val ourTrademarkAppNumbers: List<String> = listOf(),
    val tmClass: String,
    val journalNumber: String
)
