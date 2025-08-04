package com.jjeanjacques.rinhabackend.infra.httpInterface

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
class PaymentProcessorWebClient {

    @Value("\${apis.payment-processor.default.url}")
    private lateinit var baseUrl: String

    @Value("\${apis.payment-processor.fallback.url}")
    private lateinit var fallbackBaseUrl: String

    @Bean(name = ["paymentProcessorWebClientDefault"])
    fun paymentProcessorWebClientDefault(): WebClient = createWebClient(baseUrl)

    @Bean(name = ["paymentProcessorFallbackWebClient"])
    fun paymentProcessorFallbackWebClient(): WebClient = createWebClient(fallbackBaseUrl)

    private fun createWebClient(baseUrl: String): WebClient {
        val httpClient = reactor.netty.http.client.HttpClient.create()
            .responseTimeout(Duration.ofSeconds(6))

        return WebClient.builder().baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}