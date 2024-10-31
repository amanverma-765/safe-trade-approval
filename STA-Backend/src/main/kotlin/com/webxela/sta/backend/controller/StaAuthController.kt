package com.webxela.sta.backend.controller

import com.webxela.sta.backend.services.UserAuthService
import com.webxela.sta.backend.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/sta/auth")
class StaAuthController(
    private val userAuthService: UserAuthService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/register")
    suspend fun register(@RequestBody request: RegistrationRequest): Mono<ResponseEntity<String>> {
        return userAuthService.registerUser(request.email, request.password)
            .map { ResponseEntity("User registered successfully", HttpStatus.CREATED) }
            .onErrorReturn(ResponseEntity("User registration failed", HttpStatus.BAD_REQUEST))
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest): Mono<ResponseEntity<TokenResponse>> {
        return userAuthService.authenticateUser(request.email, request.password)
            .map { user ->
                val token = jwtUtil.generateToken(user.email)
                ResponseEntity(TokenResponse(token), HttpStatus.OK)
            }
            .defaultIfEmpty(ResponseEntity(TokenResponse("Invalid credentials"), HttpStatus.UNAUTHORIZED))
    }
}

data class RegistrationRequest(val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class TokenResponse(val token: String)