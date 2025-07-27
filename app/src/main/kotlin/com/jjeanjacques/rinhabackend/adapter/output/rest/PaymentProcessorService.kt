package com.jjeanjacques.rinhabackend.adapter.output.rest

import com.jjeanjacques.rinhabackend.adapter.output.rest.client.PaymentProcessorClient
import com.jjeanjacques.rinhabackend.adapter.output.rest.request.PaymentProcessorRequest
import com.jjeanjacques.rinhabackend.adapter.output.rest.response.PaymentProcessorResponse
import com.jjeanjacques.rinhabackend.adapter.output.rest.response.PaymentProcessorStatusResponse
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.exceptions.AlreadyProcessedRuntimeException
import com.jjeanjacques.rinhabackend.domain.exceptions.TimeoutRuntimeException
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.utils.toString
import io.netty.handler.codec.http.HttpResponseStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.format.DateTimeFormatter
import java.util.UUID


@Component
class PaymentProcessorService(
    private val paymentProcessorClient: PaymentProcessorClient,
    private val paymentProcessorFallbackClient: PaymentProcessorClient
) {
    suspend fun callPaymentProcessor(payment: Payment) {
        try {
            when (payment.type) {
                TypePayment.DEFAULT -> requestPaymentDefault(payment)
                TypePayment.FALLBACK -> {
                    requestFallBackPayment(payment)
                }

                TypePayment.TIMEOUT -> throw IllegalArgumentException("Unsupported payment type: ${payment.type}")

            }
        } catch (ex: WebClientResponseException) {
            log.error("WebClientResponseException calling payment processor: ${ex.message}", ex)
            if (ex.statusCode == HttpResponseStatus.UNPROCESSABLE_ENTITY) {
                throw AlreadyProcessedRuntimeException("Payment with correlation ID ${payment.correlationId} has already been processed or is invalid.")
            }
            if (ex.statusCode == HttpResponseStatus.REQUEST_TIMEOUT) {
                throw TimeoutRuntimeException("Payment request timed out for correlation ID ${payment.correlationId}.")
            }
            if (payment.type == TypePayment.DEFAULT) {
                payment.type = TypePayment.FALLBACK
                callPaymentProcessor(payment)
            }
        } catch (ex: Exception) {
            log.error("Error calling payment processor: ${ex.message}", ex)
            if (payment.type == TypePayment.DEFAULT) {
                payment.type = TypePayment.FALLBACK
                callPaymentProcessor(payment)
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
        log.warn("🔥 Falling back to fallback payment processor for correlation ID: ${payment.correlationId} 🔥")
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

    suspend fun requestPaymentById(paymentId: UUID): Payment? {
        return try {
            paymentProcessorClient.requestPaymentById(paymentId)!!
        } catch (ex: WebClientResponseException) {
            if (ex.statusCode == HttpResponseStatus.NOT_FOUND) {
                log.warn("Payment with ID $paymentId not found: ${ex.message}", ex)
            }
            null
        }
    }


    suspend fun requestPaymentProcessorStatus(type: TypePayment): PaymentProcessorStatusResponse? {
        return try {
            paymentProcessorClient.requestPaymentProcessorStatus(type)
        } catch (ex: Exception) {
            log.warn("Error requesting payment processor status for type $type: ${ex.message}", ex)
            throw ex
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
