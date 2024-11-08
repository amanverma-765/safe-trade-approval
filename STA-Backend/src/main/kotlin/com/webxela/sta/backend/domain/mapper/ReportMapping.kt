package com.webxela.sta.backend.domain.mapper

import com.webxela.sta.backend.domain.entity.OppositionReportEntity
import com.webxela.sta.backend.domain.model.OppositionReport

object ReportMapping {
    fun OppositionReportEntity.toOppositionReport(): OppositionReport {
        return OppositionReport(
            reportId = this.reportId,
            journalNumber = this.journalNumber,
            ourAppId = this.ourAppId,
            journalAppId = this.journalAppId,
            report = this.report
        )
    }

    fun OppositionReport.toOppositionReportEntity(): OppositionReportEntity {
        return OppositionReportEntity(
            journalNumber = this.journalNumber,
            ourAppId = this.ourAppId,
            journalAppId = this.journalAppId,
            report = this.report
        )
    }
}