package com.jjeanjacques.rinhabackend.adapter.rest

import com.jjeanjacques.rinhabackend.adapter.rest.client.PaymentProcessorClient
import com.jjeanjacques.rinhabackend.adapter.rest.request.PaymentProcessorRequest
import com.jjeanjacques.rinhabackend.adapter.rest.response.PaymentProcessorResponse
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.utils.toString
import org.springframework.stereotype.Component


@Component
class PaymentProcessorService(
    private val paymentProcessorClient: PaymentProcessorClient,
    private val paymentProcessorFallbackClient: PaymentProcessorClient
) {
    fun callPaymentProcessor(payment: Payment): Payment? {
        return try {
            requestPaymentDefault(payment)
            payment
        } catch (ex: Exception) {
            log.error("Error calling payment processor: \\${ex.message}", ex)
            requestFallBackPayment(payment)
            payment.type = TypePayment.FALLBACK
            payment
        }
    }

    fun requestPaymentDefault(payment: Payment): PaymentProcessorResponse? {
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

    fun requestFallBackPayment(payment: Payment): PaymentProcessorResponse? {
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
