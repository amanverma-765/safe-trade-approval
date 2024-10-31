package com.webxela.sta.backend.security

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val securityContextRepository: ServerSecurityContextRepository
) : WebFilter {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        logger.info("Authorization header: {}", authHeader)

        return if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            logger.info("JWT Token: {}", token)
            val email = jwtUtil.getEmailFromToken(token)
            logger.info("Email from token: {}", email)

            if (email != null && jwtUtil.validateToken(token)) {
                val auth = UsernamePasswordAuthenticationToken(email, null, emptyList())
                val securityContext = SecurityContextImpl(auth)
                logger.info("Security context created for user: {}", email)
                securityContextRepository.save(exchange, securityContext)
                    .then(chain.filter(exchange))
            } else {
                logger.warn("Invalid JWT token")
                clearSecurityContext(exchange)
                chain.filter(exchange)
            }
        } else {
            logger.warn("Authorization header is missing or does not start with Bearer")
            clearSecurityContext(exchange)
            chain.filter(exchange)
        }
    }

    private fun clearSecurityContext(exchange: ServerWebExchange): Mono<Void> {
        return securityContextRepository.save(exchange, SecurityContextImpl(null))
    }
}
