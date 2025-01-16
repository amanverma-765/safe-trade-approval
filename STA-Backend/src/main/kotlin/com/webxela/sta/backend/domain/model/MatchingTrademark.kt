package com.webxela.sta.backend.domain.model

data class MatchingTrademark(
    val journalNumber: String,
    val tmClass: String,
    val journalTrademark: JournalTmDataMini,
    val ourTrademarks: List<OurTmDataMini>,
) {
    data class OurTmDataMini(
        val tmApplicationNumber: String,
        val tmAppliedFor: String,
        val tmClass: String,
        val matchedSubstrings: List<String>
    )

    data class JournalTmDataMini(
        val tmApplicationNumber: String,
        val tmAppliedFor: String,
        val tmClass: String,
    )
}
