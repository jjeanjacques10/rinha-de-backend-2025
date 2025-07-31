package com.jjeanjacques.rinhabackend.infra.httpInterface

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class PaymentProcessorWebClientConfig {

    @Value("\${apis.payment-processor.default.url}")
    private lateinit var baseUrl: String

    @Value("\${apis.payment-processor.default.timeout:5000}")
    private var timeout: Long = 5000

    @Value("\${apis.payment-processor.fallback.url}")
    private lateinit var fallbackBaseUrl: String

    @Bean(name = ["paymentProcessorWebClient"])
    fun paymentProcessorWebClient(): WebClient = createWebClient(baseUrl)

    @Bean(name = ["paymentProcessorFallbackWebClient"])
    fun paymentProcessorFallbackWebClient(): WebClient = createWebClient(fallbackBaseUrl)

    private fun createWebClient(baseUrl: String): WebClient {
        val httpClient = reactor.netty.http.client.HttpClient.create()
            .responseTimeout(java.time.Duration.ofMillis(timeout))
            .doOnConnected { connection ->
                connection.addHandlerLast(io.netty.handler.timeout.ReadTimeoutHandler(timeout.toInt()))
                connection.addHandlerLast(io.netty.handler.timeout.WriteTimeoutHandler(timeout.toInt()))
            }

        return WebClient.builder().baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .clientConnector(org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
            .build()
    }
}