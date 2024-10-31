package com.webxela.sta.backend.config

import com.webxela.sta.backend.security.JwtAuthenticationFilter
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun securityContextRepository(): WebSessionServerSecurityContextRepository {
        logger.info("Creating WebSessionServerSecurityContextRepository bean")
        return WebSessionServerSecurityContextRepository()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        logger.info("Creating BCryptPasswordEncoder bean")
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityWebFilterChain {
        logger.info("Configuring SecurityWebFilterChain")
        return http
            .csrf { it.disable() }
            .cors { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/auth/**").permitAll()
                    .anyExchange().authenticated()
            }
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .securityContextRepository(securityContextRepository())
            .build()
    }
}