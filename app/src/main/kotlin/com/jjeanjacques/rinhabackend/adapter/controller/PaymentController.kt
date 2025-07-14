package com.jjeanjacques.rinhabackend.adapter.controller

import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.adapter.controller.response.PaymentResponse
import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import kotlinx.coroutines.coroutineScope
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
    ): ResponseEntity<PaymentResponse> {
        log.info("Received payment request with correlation ID: ${request.correlationId}, amount: ${request.amount}, requested at: ${request.requestedAt}")

        paymentService.validatePaymentProcessed(request)

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            paymentService.processPayment(request)
        }

        log.info("Payment processed successfully for correlation ID: ${request.correlationId}")

        return ResponseEntity.ok(
            PaymentResponse(
                status = SUCCESS_STATUS,
                message = "Payment of ${request.amount} processed successfully with correlation ID ${request.correlationId}"
            )
        )
    }

    @GetMapping("/payments-summary")
    suspend fun summaryPayments(
        @RequestParam(required = false) from: String,
        @RequestParam(required = false) to: String
    ): ResponseEntity<PaymentSummary> {
        val summary = paymentService.getSummary(from, to)
        return ResponseEntity.ok(summary)
    }

    @GetMapping("/payments/service-health")
    fun serviceHealth(): ResponseEntity<Map<String, Any>> {
        log.info("Service health check requested")
        return ResponseEntity.ok(
            mapOf(
                "failing" to false,
                "minResponseTime" to 100
            )
        )
    }

    companion object {
        private val SUCCESS_STATUS = "success"
        private const val ERROR_STATUS = "error"
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }

}