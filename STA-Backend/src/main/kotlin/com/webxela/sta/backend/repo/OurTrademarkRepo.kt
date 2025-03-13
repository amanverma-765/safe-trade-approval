package com.webxela.sta.backend.repo

import com.webxela.sta.backend.domain.entity.OurTrademarkEntity
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface OurTrademarkRepo : JpaRepository<OurTrademarkEntity, Long> {

    fun findByApplicationNumber(applicationNumber: String): OurTrademarkEntity?

    fun findByTmClass(tmClass: String): List<OurTrademarkEntity>

    @Modifying
    @Transactional
    @Query("DELETE FROM OurTrademarkEntity o WHERE o.applicationNumber = :applicationNumber")
    fun deleteByApplicationNumber(applicationNumber: String)

    @Query("SELECT t.applicationNumber FROM OurTrademarkEntity t")
    fun findAllApplicationNumbers(): List<String>

    @Modifying
    @Transactional
    @Query("UPDATE OurTrademarkEntity t SET t.status = :status WHERE t.applicationNumber = :applicationNumber")
    fun updateTrademarkStatus(applicationNumber: String, status: String): Int

}