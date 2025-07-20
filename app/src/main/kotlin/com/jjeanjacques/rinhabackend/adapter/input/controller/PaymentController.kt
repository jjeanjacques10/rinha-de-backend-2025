package com.jjeanjacques.rinhabackend.adapter.input.controller

import com.jjeanjacques.rinhabackend.adapter.input.controller.response.PaymentResponse
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import org.slf4j.LoggerFactory
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

    companion object {
        private val SUCCESS_STATUS = "success"
        private const val ERROR_STATUS = "error"
        private val log = LoggerFactory.getLogger(this::class.java)
    }

}