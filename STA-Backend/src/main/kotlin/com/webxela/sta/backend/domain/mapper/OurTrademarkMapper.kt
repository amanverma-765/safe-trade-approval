package com.webxela.sta.backend.domain.mapper

import com.webxela.sta.backend.domain.entity.OurTrademarkEntity
import com.webxela.sta.backend.domain.model.Trademark

object OurTrademarkMapper {

    fun OurTrademarkEntity.toTrademark(): Trademark {
        return Trademark(
            tmId = this.tmId,
            state = this.state,
            proprietorName = this.proprietorName,
            status = this.status,
            proprietorAddress = this.proprietorAddress,
            publicationDetails = this.publicationDetails,
            tmClass = this.tmClass,
            agentAddress = this.agentAddress,
            userDetails = this.userDetails,
            agentName = this.agentName,
            applicationNumber = this.applicationNumber,
            appropriateOffice = this.appropriateOffice,
            dateOfApplication = this.dateOfApplication,
            tmAppliedFor = this.tmAppliedFor,
            emailId = this.emailId,
            validUpTo = this.validUpTo,
            certDetail = this.certDetail,
            tmType = this.tmType,
            country = this.country,
            filingMode = this.filingMode,
            tmCategory = this.tmCategory
        )
    }

    fun Trademark.toOurTrademarkEntity(): OurTrademarkEntity {
        return OurTrademarkEntity(
            state = this.state,
            proprietorName = this.proprietorName,
            status = this.status,
            proprietorAddress = this.proprietorAddress,
            publicationDetails = this.publicationDetails,
            tmClass = this.tmClass,
            agentAddress = this.agentAddress,
            userDetails = this.userDetails,
            agentName = this.agentName,
            applicationNumber = this.applicationNumber,
            appropriateOffice = this.appropriateOffice,
            dateOfApplication = this.dateOfApplication,
            tmAppliedFor = this.tmAppliedFor,
            emailId = this.emailId,
            validUpTo = this.validUpTo,
            certDetail = this.certDetail,
            tmType = this.tmType,
            country = this.country,
            filingMode = this.filingMode,
            tmCategory = this.tmCategory
        )
    }
}