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
import java.time.Instant

@RestController
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/payments")
    suspend fun processPayment(
        @RequestBody request: Payment
    ): PaymentResponse {
        CoroutineScope(Dispatchers.IO).launch {
            log.info("[${request.correlationId}] Received payment request, request: $request")
            paymentService.sendToProcessor(request)
        }

        return PaymentResponse(status = SUCCESS)
    }

    @GetMapping("/payments-summary")
    suspend fun summaryPayments(
        @RequestParam(required = false) from: String?,
        @RequestParam(required = false) to: String?
    ): PaymentSummary? {
        return paymentService.getSummary(
            from ?: Instant.now().minusSeconds(3600).toString(),
            to ?: Instant.now().toString()
        )
    }

    companion object {
        private const val SUCCESS = "success"
        private val log = LoggerFactory.getLogger(this::class.java)
    }

}