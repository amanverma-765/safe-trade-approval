package com.webxela.sta.backend.repo

import com.webxela.sta.backend.domain.entity.OurTrademarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OurTrademarkRepo: JpaRepository<OurTrademarkEntity, Long> {

    fun findByApplicationNumber(applicationNumber: String): OurTrademarkEntity?

    fun findByTmClass(tmClass: String): List<OurTrademarkEntity>
}