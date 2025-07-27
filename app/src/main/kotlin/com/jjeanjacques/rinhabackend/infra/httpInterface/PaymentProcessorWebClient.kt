package com.jjeanjacques.rinhabackend.infra.httpInterface

import com.jjeanjacques.rinhabackend.adapter.output.rest.client.PaymentProcessorClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

//@Configuration
class PaymentProcessorWebClient {

    @Value("\${apis.payment-processor.default.url}")
    private lateinit var baseUrl: String

    @Value("\${apis.payment-processor.default.timeout:5000}")
    private var timeout: Long = 5000

    @Value("\${apis.payment-processor.fallback.url}")
    private lateinit var fallbackBaseUrl: String

    @Value("\${apis.payment-processor.fallback.timeout:5000}")
    private var fallbackTimeout: Long = 5000

    @Bean
    fun paymentProcessorClient(): PaymentProcessorClient {
        val webClient = WebClient.builder().baseUrl(baseUrl).defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
//            .clientConnector(
//                org.springframework.http.client.reactive.ReactorClientHttpConnector(
//                    reactor.netty.http.client.HttpClient.create()
//                        .responseTimeout(java.time.Duration.ofMillis(timeout))
//                )
//            )
            .build()
        val adapter = WebClientAdapter.create(webClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(PaymentProcessorClient::class.java)
    }

    @Bean
    fun paymentProcessorFallbackClient(): PaymentProcessorClient {
        val webClient = WebClient.builder().baseUrl(fallbackBaseUrl).defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json").clientConnector(
                org.springframework.http.client.reactive.ReactorClientHttpConnector(
                    reactor.netty.http.client.HttpClient.create()
                        .responseTimeout(java.time.Duration.ofMillis(fallbackTimeout))
                )
            ).build()
        val adapter = WebClientAdapter.create(webClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()

        return factory.createClient(PaymentProcessorClient::class.java)
    }
}

@Configuration
class PaymentProcessorWebClientConfig {


    @Value("\${apis.payment-processor.default.url}")
    private lateinit var baseUrl: String

    @Value("\${apis.payment-processor.default.timeout:250}")
    private var timeout: Long = 5000

    @Value("\${apis.payment-processor.fallback.url}")
    private lateinit var fallbackBaseUrl: String

    @Bean(name = ["paymentProcessorWebClient"])
    fun paymentProcessorWebClient(): WebClient = createWebClient(baseUrl)

    @Bean(name = ["paymentProcessorFallbackWebClient"])
    fun paymentProcessorFallbackWebClient(): WebClient = createWebClient(fallbackBaseUrl)

    private fun createWebClient(baseUrl: String): WebClient {
        return WebClient.builder().baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json").clientConnector(
                org.springframework.http.client.reactive.ReactorClientHttpConnector(
                    reactor.netty.http.client.HttpClient.create().responseTimeout(java.time.Duration.ofMillis(timeout))
                )
            ).build()
    }
}