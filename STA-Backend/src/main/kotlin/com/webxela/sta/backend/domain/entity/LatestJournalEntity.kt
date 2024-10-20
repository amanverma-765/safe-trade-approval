package com.webxela.sta.backend.domain.entity

import jakarta.persistence.*

@Entity
@Table(name = "latest_journal")
data class LatestJournalEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journalId")
    val journalId: Long = 0,

    @Column(name = "journal_number")
    val journalNumber: String,

    @Column(name = "date_of_publication")
    val dateOfPublication: String,

    @Column(name = "date_of_availability")
    val dateOfAvailability: String,
)
