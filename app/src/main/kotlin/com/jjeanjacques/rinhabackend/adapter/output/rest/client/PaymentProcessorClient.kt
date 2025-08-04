package com.jjeanjacques.rinhabackend.adapter.output.rest.client

import com.jjeanjacques.rinhabackend.adapter.output.rest.request.PaymentProcessorRequest
import com.jjeanjacques.rinhabackend.adapter.output.rest.response.PaymentProcessorResponse
import com.jjeanjacques.rinhabackend.adapter.output.rest.response.PaymentProcessorStatusResponse
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import io.netty.handler.codec.http.HttpResponseStatus
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PaymentProcessorClient(
    @Qualifier("paymentProcessorWebClientDefault")
    private val webClient: WebClient,
    @Qualifier("paymentProcessorFallbackWebClient")
    private val webClientFallback: WebClient
) {

    suspend fun requestPaymentProcessorDefault(request: PaymentProcessorRequest): PaymentProcessorResponse? =
        webClient.post()
            .uri("/payments")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PaymentProcessorResponse::class.java)
            .awaitSingle()

    suspend fun requestPaymentProcessorFallback(request: PaymentProcessorRequest): PaymentProcessorResponse? =
        webClientFallback.post()
            .uri("/payments")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PaymentProcessorResponse::class.java)
            .awaitSingle()


    suspend fun requestPaymentProcessorStatus(
        type: TypePayment
    ): PaymentProcessorStatusResponse? {
        return try {
            when (type) {
                TypePayment.FALLBACK -> webClientFallback.get()
                    .uri("/payments/service-health")
                    .retrieve()
                    .bodyToMono(PaymentProcessorStatusResponse::class.java)
                    .awaitSingle()

                else -> webClient.get()
                    .uri("/payments/service-health")
                    .retrieve()
                    .bodyToMono(PaymentProcessorStatusResponse::class.java)
                    .awaitSingle()
            }
        } catch (ex: WebClientResponseException) {
            if (ex.statusCode == HttpResponseStatus.TOO_MANY_REQUESTS) {
                log.warn("Rate limit exceeded for payment processor")
            } else {
                log.error("Error requesting payment processor status: ${ex.message}", ex)
            }
            return null
        } catch (ex: Exception) {
            throw RuntimeException("Error requesting payment processor status: ${ex.message}", ex)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(PaymentProcessorClient::class.java)
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