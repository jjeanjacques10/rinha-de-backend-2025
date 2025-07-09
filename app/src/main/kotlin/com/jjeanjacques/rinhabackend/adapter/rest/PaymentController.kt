package com.jjeanjacques.rinhabackend.adapter.rest

import com.jjeanjacques.rinhabackend.adapter.rest.response.PaymentDetailsResponse
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.adapter.rest.response.PaymentResponse
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping
    fun processPayment(
        @RequestBody request: Payment,
        @RequestHeader("X-Rinha-Token") tokenRinha: String?
    ): ResponseEntity<PaymentResponse> {
        log.info("Received payment request with correlation ID: ${request.correlationId}, amount: ${request.amount}, requested at: ${request.requestedAt}")

        if (tokenRinha.isNullOrBlank()) {
            log.warn("Missing or invalid X-Rinha-Token header")
            return ResponseEntity.badRequest().body(
                PaymentResponse(
                    status = ERROR_STATUS,
                    message = "Missing or invalid X-Rinha-Token header"
                )
            )
        }

        paymentService.processPayment(request)

        return ResponseEntity.ok(
            PaymentResponse(
                status = SUCCESS_STATUS,
                message = "Payment of ${request.amount} processed successfully with correlation ID ${request.correlationId}"
            )
        )
    }

    @GetMapping("/{id}")
    fun getPaymentStatus(@PathVariable id: UUID): ResponseEntity<PaymentDetailsResponse> {
        log.info("Checking payment status for ID: $id")
        return ResponseEntity.ok(
            paymentService.getPayment(id).let { payment ->
                PaymentDetailsResponse(
                    correlationId = payment.correlationId,
                    amount = payment.amount,
                    requested_at = payment.requestedAt
                )
            }
        )
    }

    companion object {
        private val SUCCESS_STATUS = "success"
        private const val ERROR_STATUS = "error"
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }

}