package com.webxela.sta.backend.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "opposition_report")
data class OppositionReportEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    val reportId: Long? = null,

    @Column(name = "journal_number")
    val journalNumber: String,

    @Column(name = "our_app_id")
    val ourAppId: String,

    @Column(name = "journal_app_id")
    val journalAppId: String,

    @Column(name = "report")
    val report: String
)
