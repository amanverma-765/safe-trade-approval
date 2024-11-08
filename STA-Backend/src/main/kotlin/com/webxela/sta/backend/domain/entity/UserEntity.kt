package com.webxela.sta.backend.domain.entity

import jakarta.persistence.*


@Entity
@Table(name = "user_auth")
data class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true)
    val email: String,
    var password: String
)

