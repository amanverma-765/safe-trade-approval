package com.webxela.sta.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono

@Configuration
class WebFluxConfig : WebFluxConfigurer {
    @Bean
    fun reactiveAuthenticationManager(): ReactiveAuthenticationManager {
        return ReactiveAuthenticationManager { authentication ->
            Mono.just(authentication)
                .map { auth ->
                    auth.isAuthenticated = true
                    auth
                }
        }
    }
}