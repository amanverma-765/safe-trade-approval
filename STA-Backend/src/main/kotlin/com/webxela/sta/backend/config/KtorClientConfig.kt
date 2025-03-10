package com.webxela.sta.backend.config

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Configuration
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@Configuration
class KtorClientConfig {

    fun createHttpClient(
        ignoreUnknownKeys: Boolean = true,
        isLenient: Boolean = true
    ): HttpClient {
        return HttpClient(CIO) {
            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        this.ignoreUnknownKeys = ignoreUnknownKeys
                        this.isLenient = isLenient
                    }
                )
            }
            install(HttpTimeout)
            {
                 requestTimeoutMillis = 20000
                 connectTimeoutMillis = 20000
                 socketTimeoutMillis = 20000
            }
            engine {
                https {
                    // Disable SSL certificate validation
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    }
                }
            }
        }
    }
}
