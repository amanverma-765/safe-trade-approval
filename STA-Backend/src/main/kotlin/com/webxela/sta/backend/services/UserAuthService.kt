package com.webxela.sta.backend.services

import com.webxela.sta.backend.domain.mapper.UserMapper.toUser
import com.webxela.sta.backend.domain.mapper.UserMapper.toUserEntity
import com.webxela.sta.backend.domain.model.User
import com.webxela.sta.backend.repo.UserAuthRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserAuthService(
    private val userAuthRepo: UserAuthRepo,
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun registerUser(email: String, rawPassword: String): Mono<User> {
        val encodedPassword = passwordEncoder.encode(rawPassword)
        val user = User(email = email, password = encodedPassword)
        return withContext(Dispatchers.IO) {
            Mono.just(userAuthRepo.save(user.toUserEntity()).toUser())
        }
    }

    suspend fun authenticateUser(email: String, rawPassword: String): Mono<User> {
        return withContext(Dispatchers.IO) {
            userAuthRepo.findByEmail(email)
                ?.let { userEntity ->
                    if (passwordEncoder.matches(rawPassword, userEntity.password)) {
                        Mono.just(userEntity.toUser())
                    } else {
                        Mono.empty()
                    }
                } ?: Mono.empty()
        }
    }
}