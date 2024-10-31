package com.webxela.sta.backend.domain.model

data class User(
    val id: Long? = null,
    val email: String,
    var password: String
)
