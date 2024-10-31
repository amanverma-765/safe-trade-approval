package com.webxela.sta.backend.repo

import com.webxela.sta.backend.domain.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserAuthRepo : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
}