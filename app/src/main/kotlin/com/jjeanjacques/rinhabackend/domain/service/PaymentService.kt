package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.adapter.rest.PaymentProcessorService
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.DefaultDetails
import com.jjeanjacques.rinhabackend.domain.models.FallbackDetails
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PaymentService(
    private val paymentProcessorDefault: PaymentProcessorService,
    private val paymentRepository: PaymentRepository
) {
    suspend fun processPayment(payment: Payment) {
        log.info("Requesting payment with correlation ID: ${payment.correlationId}, amount: ${payment.amount}, requested at: ${payment.requestedAt}")
        paymentProcessorDefault.callPaymentProcessor(payment)
        paymentRepository.save(payment)
    }

    fun validatePaymentProcessed(payment: Payment) {
        if (paymentRepository.checkExists(payment.correlationId)) {
            log.info("Payment with correlation ID: ${payment.correlationId} already processed.")
            throw RuntimeException(
                "Payment with correlation ID: ${payment.correlationId} already processed."
            )
        }
    }

    fun getSummary(from: String, to: String): PaymentSummary? {
        val fromInstant = Instant.parse(if (from.endsWith("Z")) from else "${from}Z")
        val toInstant = Instant.parse(if (to.endsWith("Z")) to else "${to}Z")

        val payments = paymentRepository.getAll().filter {
            it.requestedAt != null &&
                    it.requestedAt!!.isAfter(fromInstant) &&
                    it.requestedAt!!.isBefore(toInstant)
        }

        val paymentsDefault = payments.filter { it.type == TypePayment.DEFAULT }
        val paymentsFallback = payments.filter { it.type == TypePayment.FALLBACK }

        log.info(
            "Payments summary from $from to $to: " +
                    "Default requests: ${paymentsDefault.size}, " +
                    "Total amount: ${paymentsDefault.sumOf { it.amount }}, " +
                    "Fallback requests: ${paymentsFallback.size}, " +
                    "Total amount: ${paymentsFallback.sumOf { it.amount }}"
        )

        return PaymentSummary(
            default = DefaultDetails(
                totalRequests = paymentsDefault.size,
                totalAmount = paymentsDefault.sumOf { it.amount }
            ),
            fallback = FallbackDetails(
                totalRequests = paymentsFallback.size,
                totalAmount = paymentsFallback.sumOf { it.amount }
            )
        )
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}