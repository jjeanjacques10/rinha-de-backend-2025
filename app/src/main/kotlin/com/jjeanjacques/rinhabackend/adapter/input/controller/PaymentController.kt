package com.jjeanjacques.rinhabackend.adapter.input.controller

import com.jjeanjacques.rinhabackend.adapter.input.controller.response.PaymentResponse
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        CoroutineScope(Dispatchers.Default).launch {
            log.debug("Received payment request with correlation ID: ${request.correlationId}, amount: ${request.amount}, requested at: ${request.requestedAt}")
            paymentService.sendToProcessor(request)
        }

        return PaymentResponse(
            status = "success",
            message = "Payment received and will be processed asynchronously"
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
        private val log = LoggerFactory.getLogger(this::class.java)
    }

}