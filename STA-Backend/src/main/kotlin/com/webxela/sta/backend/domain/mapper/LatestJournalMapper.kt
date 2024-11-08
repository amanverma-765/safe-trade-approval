package com.webxela.sta.backend.domain.mapper

import com.webxela.sta.backend.domain.entity.LatestJournalEntity
import com.webxela.sta.backend.domain.model.LatestJournal

object LatestJournalMapper {

    fun LatestJournalEntity.toJournal(): LatestJournal {
        return LatestJournal(
            journalId = this.journalId,
            journalNumber = this.journalNumber,
            dateOfPublication = this.dateOfPublication,
            dateOfAvailability = this.dateOfAvailability,
        )
    }

    fun LatestJournal.toJournalEntity(): LatestJournalEntity {
        return LatestJournalEntity(
            journalNumber = this.journalNumber,
            dateOfPublication = this.dateOfPublication,
            dateOfAvailability = this.dateOfAvailability
        )
    }
}