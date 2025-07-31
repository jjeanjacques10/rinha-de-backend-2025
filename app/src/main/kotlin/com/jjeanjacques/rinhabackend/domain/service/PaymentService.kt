package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.adapter.output.rest.PaymentProcessorService
import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.exceptions.AlreadyProcessedRuntimeException
import com.jjeanjacques.rinhabackend.domain.exceptions.IntegrationException
import com.jjeanjacques.rinhabackend.domain.exceptions.TimeoutRuntimeException
import com.jjeanjacques.rinhabackend.domain.models.DefaultDetails
import com.jjeanjacques.rinhabackend.domain.models.FallbackDetails
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PaymentService(
    private val paymentProcessorService: PaymentProcessorService,
    private val paymentProducerPort: PaymentProducerPort,
    private val paymentRepository: PaymentRepository
) {

    suspend fun sendToProcessor(payment: Payment) {
        log.debug("Sending payment with correlation ID: ${payment.correlationId} to processor.")
        paymentProducerPort.send(payment, StatusPayment.PENDING)
    }

    suspend fun processPayment(payment: Payment): StatusPayment {
        try {
            log.info("Requesting payment with correlation ID: ${payment.correlationId}, amount: ${payment.amount}, requested at: ${payment.requestedAt}")

            val statusPayment = getStatusProcess(payment)

            log.info("Payment status for correlation ID: ${payment.correlationId} is $statusPayment")
            when (statusPayment) {
                StatusPayment.SUCCESS -> paymentRepository.save(payment, statusPayment)
                StatusPayment.ERROR -> log.warn("Ignoring payment with correlation ID: ${payment.correlationId} due to error status.")
                else -> paymentProducerPort.send(payment, statusPayment)
            }
            return statusPayment
        } catch (ex: Exception) {
            log.error("Failed to process pending payment with correlation ID: ${payment.correlationId}", ex)
        }
        return StatusPayment.ERROR
    }

    private suspend fun getStatusProcess(payment: Payment) = try {
        paymentProcessorService.callPaymentProcessor(payment)
        StatusPayment.SUCCESS
    } catch (_: AlreadyProcessedRuntimeException) {
        log.info("Payment with correlation ID: ${payment.correlationId} already processed, skipping.")
        StatusPayment.SUCCESS
    } catch (_: TimeoutRuntimeException) {
        log.error("Payment processor timed out for correlation ID: ${payment.correlationId}")
        StatusPayment.PENDING
    } catch (_: IntegrationException) {
        log.error("Payment processor is unavailable for correlation ID: ${payment.correlationId}")
        StatusPayment.ERROR
    } catch (ex: Exception) {
        log.error("Payment processor failed for correlation ID: ${payment.correlationId}")
        StatusPayment.ERROR
    }

    suspend fun getSummary(from: String, to: String): PaymentSummary? {
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