package com.webxela.sta.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.webxela.sta.backend.utils.Constants.JWT_TOKEN
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {
    private val logger = LoggerFactory.getLogger(JwtUtil::class.java)
    private val secret = JWT_TOKEN

    fun generateToken(email: String): String {
        return JWT.create()
            .withSubject(email)
            .withExpiresAt(Date(System.currentTimeMillis() + 86400000))
            .sign(Algorithm.HMAC256(secret))
    }

    fun validateToken(token: String): Boolean {
        return try {
            val verifier = JWT.require(Algorithm.HMAC256(secret)).build()
            verifier.verify(token)
            true
        } catch (e: Exception) {
            logger.warn("Token validation failed: ${e.message}")
            false
        }
    }

    fun getEmailFromToken(token: String): String? {
        return try {
            val verifier = JWT.require(Algorithm.HMAC256(secret)).build()
            val decodedJWT = verifier.verify(token)
            decodedJWT.subject
        } catch (e: Exception) {
            logger.warn("Error extracting email from token: ${e.message}")
            null
        }
    }
}