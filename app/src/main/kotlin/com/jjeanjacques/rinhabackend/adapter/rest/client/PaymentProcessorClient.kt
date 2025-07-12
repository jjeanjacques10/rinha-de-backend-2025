package com.jjeanjacques.rinhabackend.adapter.rest.client

import com.jjeanjacques.rinhabackend.adapter.rest.request.PaymentProcessorRequest
import com.jjeanjacques.rinhabackend.adapter.rest.response.PaymentProcessorResponse
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

// https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface
interface PaymentProcessorClient {
    @PostExchange("/payments")
    fun requestPaymentProcessorDefault(
        @RequestBody paymentProcessorRequest: PaymentProcessorRequest
    ): PaymentProcessorResponse?
}