package com.webxela.sta.backend.domain.mapper

import com.webxela.sta.backend.domain.entity.UserEntity
import com.webxela.sta.backend.domain.model.User

object UserMapper {
    fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id = this.id ?: 0,
            email = this.email,
            password = this.password
        )
    }

    fun UserEntity.toUser(): User {
        return User(
            id = this.id,
            email = this.email,
            password = this.password
        )
    }
}