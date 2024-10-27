package com.webxela.sta.backend.repo

import com.webxela.sta.backend.domain.entity.OurTrademarkEntity
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OurTrademarkRepo: JpaRepository<OurTrademarkEntity, Long> {

    fun findByApplicationNumber(applicationNumber: String): OurTrademarkEntity?

    fun findByTmClass(tmClass: String): List<OurTrademarkEntity>

//    fun findByTmClassAndTmAppliedForContainingIgnoreCase(tmClass: String, tmAppliedFor: String): List<OurTrademarkEntity>

    @Modifying
    @Transactional
    @Query("DELETE FROM OurTrademarkEntity o WHERE o.applicationNumber = :applicationNumber")
    fun deleteByApplicationNumber(applicationNumber: String)

    fun findByApplicationNumberIn(tmNumberList: List<String>)

    @Query("SELECT t.applicationNumber FROM OurTrademarkEntity t")
    fun findAllApplicationNumbers(): List<String>

}