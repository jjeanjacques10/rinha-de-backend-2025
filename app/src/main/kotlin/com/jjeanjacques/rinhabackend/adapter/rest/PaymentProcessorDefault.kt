package com.jjeanjacques.rinhabackend.adapter.rest

import com.jjeanjacques.rinhabackend.adapter.rest.client.PaymentProcessorClient
import com.jjeanjacques.rinhabackend.adapter.rest.request.PaymentProcessorRequest
import com.jjeanjacques.rinhabackend.adapter.rest.response.PaymentDefaultResponse
import com.jjeanjacques.rinhabackend.domain.models.Payment
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.format.DateTimeFormatter

@Component
class PaymentProcessorDefault(
    private val paymentProcessorClient: PaymentProcessorClient,
    private val paymentProcessorFallbackClient: PaymentProcessorClient
) {
    fun callPaymentProcessor(payment: Payment): PaymentDefaultResponse? {
        return try {
            requestPaymentDefault(payment)
        } catch (ex: Exception) {
            log.error("Error calling payment processor: \\${ex.message}", ex)
            requestFallBackPayment(payment)
        }
    }

    fun requestPaymentDefault(payment: Payment): PaymentDefaultResponse? {
        return paymentProcessorClient.requestPaymentProcessorDefault(
            PaymentProcessorRequest(
                correlationId = payment.correlationId,
                amount = payment.amount,
                requestedAt = payment.requestedAt!!.toString(
                    java.time.format.DateTimeFormatter.ISO_INSTANT
                )
            )
        )
    }

    fun requestFallBackPayment(payment: Payment): PaymentDefaultResponse? {
        log.warn("🔥 Falling back to fallback payment processor for correlation ID: \\${payment.correlationId} 🔥")
        return paymentProcessorFallbackClient.requestPaymentProcessorDefault(
            PaymentProcessorRequest(
                correlationId = payment.correlationId,
                amount = payment.amount,
                requestedAt = payment.requestedAt!!.toString(
                    java.time.format.DateTimeFormatter.ISO_INSTANT
                )
            )
        )
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

private fun Instant.toString(isoInstant: DateTimeFormatter): String {
    return isoInstant.format(this)
}
