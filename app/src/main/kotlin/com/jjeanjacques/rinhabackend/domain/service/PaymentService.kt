package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.adapter.rest.PaymentProcessorDefault
import com.jjeanjacques.rinhabackend.domain.models.DefaultDetails
import com.jjeanjacques.rinhabackend.domain.models.FallbackDetails
import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.AdminPaymentRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class PaymentService(
    private val adminPaymentRepository: AdminPaymentRepository,
    private val paymentProcessorDefault: PaymentProcessorDefault
) {
    fun processPayment(request: Payment) {
        log.info("Requesting payment with correlation ID: ${request.correlationId}, amount: ${request.amount}, requested at: ${request.requestedAt}")
        val response = paymentProcessorDefault.callPaymentProcessor(request)
        log.info("[${request.correlationId}] Payment processor response: $response")
    }

    fun getSummary(from: String, to: String): PaymentSummary? {
        val payments = adminPaymentRepository.getPaymentsByRange(
            Instant.parse(if (from.endsWith("Z")) from else "${from}Z"),
            Instant.parse(if (to.endsWith("Z")) to else "${to}Z")
        )

        log.info("Retrieved ${payments.size} payments from $from to $to")

        return PaymentSummary(
            default = DefaultDetails(
                totalRequests = payments.size,
                totalAmount = payments.sumOf { it.amount }
            ),
            fallback = FallbackDetails(
                totalRequests = 0,
                totalAmount = BigDecimal.ZERO
            )
        )
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}