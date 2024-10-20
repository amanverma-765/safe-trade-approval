package com.webxela.sta.backend.repo

import com.webxela.sta.backend.domain.entity.LatestJournalEntity
import com.webxela.sta.backend.domain.entity.OurTrademarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LatestJournalRepo: JpaRepository<LatestJournalEntity, Long> {
    @Query("SELECT MAX(journalNumber) FROM LatestJournalEntity")
    fun findLastJournalNumber(): String?

}