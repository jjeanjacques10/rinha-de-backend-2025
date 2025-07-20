package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.adapter.output.rest.PaymentProcessorService
import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.exceptions.AlreadyProcessedRuntimeException
import com.jjeanjacques.rinhabackend.domain.models.DefaultDetails
import com.jjeanjacques.rinhabackend.domain.models.FallbackDetails
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PaymentService(
    private val paymentProcessorService: PaymentProcessorService,
    private val paymentRepository: PaymentRepository,
    private val validateStatusPort: ValidateStatusPort
) {
    suspend fun processPayment(payment: Payment): StatusPayment {
        log.info("Requesting payment with correlation ID: ${payment.correlationId}, amount: ${payment.amount}, requested at: ${payment.requestedAt}")

        var statusPayment: StatusPayment

        val paymentType = validateStatusPort.canProcessPayment()

        if (paymentType != null) {
            statusPayment = try {
                paymentProcessorService.callPaymentProcessor(payment, paymentType)
                StatusPayment.SUCCESS
            } catch (e: Exception) {
                log.error("Payment processor failed for correlation ID: ${payment.correlationId}, falling back to fallback processor.")
                StatusPayment.PENDING
            }
        } else {
            log.error("Payment processor is not available for correlation ID: ${payment.correlationId}")
            statusPayment = StatusPayment.PENDING
        }

        log.info("Payment status for correlation ID: ${payment.correlationId} is $statusPayment")
        paymentRepository.save(payment, statusPayment)
        return statusPayment
    }

    suspend fun processPendingPayments() {
        log.info("Processing pending payments...")

        val pendingPayments = paymentRepository.getPendentPayments()
        if (pendingPayments.isEmpty()) {
            log.info("No pending payments to process.")
            return
        }

        pendingPayments.forEach { payment ->
            try {
                log.info("Processing pending payment with correlation ID: ${payment.correlationId}")

                validatePaymentProcessed(payment)

                val status = processPayment(payment)
                if (status == StatusPayment.SUCCESS) {
                    paymentRepository.delete(payment.correlationId, StatusPayment.PENDING)
                }
            } catch (e: Exception) {
                if (e is AlreadyProcessedRuntimeException) {
                    paymentRepository.delete(payment.correlationId, StatusPayment.PENDING)
                    paymentRepository.save(payment, StatusPayment.SUCCESS)
                    return@forEach
                }
                log.error("Failed to process pending payment with correlation ID: ${payment.correlationId}", e)
            }
        }
    }

    fun validatePaymentProcessed(payment: Payment) {
        if (paymentRepository.checkExists(payment.correlationId)) {
            log.info("Payment with correlation ID: ${payment.correlationId} already processed.")
            throw AlreadyProcessedRuntimeException(
                "Payment with correlation ID: ${payment.correlationId} already processed."
            )
        }
    }

    fun getSummary(from: String, to: String): PaymentSummary? {
        val fromInstant = Instant.parse(if (from.endsWith("Z")) from else "${from}Z")
        val toInstant = Instant.parse(if (to.endsWith("Z")) to else "${to}Z")

        val payments = paymentRepository.findByDateRange(fromInstant, toInstant)

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
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}