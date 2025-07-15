package com.jjeanjacques.rinhabackend.adapter.rest.client

import com.jjeanjacques.rinhabackend.adapter.rest.request.PaymentProcessorRequest
import com.jjeanjacques.rinhabackend.adapter.rest.response.PaymentProcessorResponse
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PaymentProcessorClient(
    @Qualifier("paymentProcessorWebClient")
    private val webClient: WebClient,
    @Qualifier("paymentProcessorFallbackWebClient")
    private val webClientFallback: WebClient
) {

    suspend fun requestPaymentProcessorDefault(
        paymentProcessorRequest: PaymentProcessorRequest
    ): PaymentProcessorResponse? {
        return webClient.post()
            .uri("/payments")
            .bodyValue(paymentProcessorRequest)
            .retrieve()
            .bodyToMono(PaymentProcessorResponse::class.java)
            .awaitSingle()
    }

    suspend fun requestPaymentProcessorFallback(
        paymentProcessorRequest: PaymentProcessorRequest
    ): PaymentProcessorResponse? {
        return webClientFallback.post()
            .uri("/payments")
            .bodyValue(paymentProcessorRequest)
            .retrieve()
            .bodyToMono(PaymentProcessorResponse::class.java)
            .awaitSingle()
    }


}


// Exemple of a REST client interface using Spring's WebClient
// https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface
//interface PaymentProcessorClient {
//    @PostExchange("/payments")
//    fun requestPaymentProcessorDefault(
//        @RequestBody paymentProcessorRequest: PaymentProcessorRequest
//    ): PaymentProcessorResponse?
//}