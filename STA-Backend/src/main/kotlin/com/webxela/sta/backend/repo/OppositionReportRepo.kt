package com.webxela.sta.backend.repo

import com.webxela.sta.backend.domain.entity.OppositionReportEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OppositionReportRepo: JpaRepository<OppositionReportEntity, Long> {

}