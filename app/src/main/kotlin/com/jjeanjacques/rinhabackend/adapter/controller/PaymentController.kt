package com.jjeanjacques.rinhabackend.adapter.controller

import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.adapter.controller.response.PaymentResponse
import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/payments")
    suspend fun processPayment(
        @RequestBody request: Payment
    ): PaymentResponse {
        log.info("Received payment request with correlation ID: ${request.correlationId}, amount: ${request.amount}, requested at: ${request.requestedAt}")

        paymentService.validatePaymentProcessed(request)

        paymentService.processPayment(request)

        log.info("Payment processed successfully for correlation ID: ${request.correlationId}")

        return PaymentResponse(
            status = SUCCESS_STATUS,
            message = "Payment of ${request.amount} processed successfully with correlation ID ${request.correlationId}"
        )
    }

    @GetMapping("/payments-summary")
    suspend fun summaryPayments(
        @RequestParam(required = false) from: String,
        @RequestParam(required = false) to: String
    ): PaymentSummary? {
        return paymentService.getSummary(from, to)
    }

    @GetMapping("/payments/service-health")
    fun serviceHealth(): Map<String, Any> {
        log.info("Service health check requested")
        return mapOf(
            "failing" to false,
            "minResponseTime" to 100
        )
    }

    companion object {
        private val SUCCESS_STATUS = "success"
        private const val ERROR_STATUS = "error"
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }

}