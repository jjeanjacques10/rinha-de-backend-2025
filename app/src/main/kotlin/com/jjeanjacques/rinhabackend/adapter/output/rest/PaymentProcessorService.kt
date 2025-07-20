package com.jjeanjacques.rinhabackend.adapter.output.rest

import com.jjeanjacques.rinhabackend.adapter.output.rest.client.PaymentProcessorClient
import com.jjeanjacques.rinhabackend.adapter.output.rest.request.PaymentProcessorRequest
import com.jjeanjacques.rinhabackend.adapter.output.rest.response.PaymentProcessorResponse
import com.jjeanjacques.rinhabackend.adapter.output.rest.response.PaymentProcessorStatusResponse
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.utils.toString
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter


@Component
class PaymentProcessorService(
    private val paymentProcessorClient: PaymentProcessorClient,
    private val paymentProcessorFallbackClient: PaymentProcessorClient
) {
    suspend fun callPaymentProcessor(payment: Payment, paymentType: TypePayment) {
        try {
            when (paymentType) {
                TypePayment.DEFAULT -> requestPaymentDefault(payment)
                TypePayment.FALLBACK -> {
                    payment.type = TypePayment.FALLBACK
                    requestFallBackPayment(payment)
                }

                TypePayment.TIMEOUT -> throw IllegalArgumentException("Unsupported payment type: $paymentType")

            }
        } catch (ex: Exception) {
            log.error("Error calling payment processor: ${ex.message}", ex)
            payment.type = TypePayment.FALLBACK
            if (paymentType == TypePayment.DEFAULT) {
                requestFallBackPayment(payment)
            }
        }
    }

    suspend fun requestPaymentDefault(payment: Payment): PaymentProcessorResponse? {
        return paymentProcessorClient.requestPaymentProcessorDefault(
            PaymentProcessorRequest(
                correlationId = payment.correlationId,
                amount = payment.amount,
                requestedAt = payment.requestedAt!!.toString(
                    DateTimeFormatter.ISO_INSTANT
                )
            )
        )
    }

    suspend fun requestFallBackPayment(payment: Payment): PaymentProcessorResponse? {
        log.warn("🔥 Falling back to fallback payment processor for correlation ID: \\${payment.correlationId} 🔥")
        return paymentProcessorFallbackClient.requestPaymentProcessorFallback(
            PaymentProcessorRequest(
                correlationId = payment.correlationId,
                amount = payment.amount,
                requestedAt = payment.requestedAt!!.toString(
                    DateTimeFormatter.ISO_INSTANT
                )
            )
        )
    }


    suspend fun requestPaymentProcessorStatus(type: TypePayment): PaymentProcessorStatusResponse {
        return try {
            paymentProcessorClient.requestPaymentProcessorStatus(type)!!
        } catch (ex: Exception) {
            log.warn("Error requesting payment processor status for type $type: ${ex.message}", ex)
            throw ex
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
