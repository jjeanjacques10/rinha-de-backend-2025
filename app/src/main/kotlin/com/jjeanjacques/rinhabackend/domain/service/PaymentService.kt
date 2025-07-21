package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.adapter.output.rest.PaymentProcessorService
import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.exceptions.AlreadyProcessedRuntimeException
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
    private val paymentRepository: PaymentRepository,
    private val validateService: ValidateService
) {

    suspend fun sendToProcessor(payment: Payment) {
        log.info("Sending payment with correlation ID: ${payment.correlationId} to processor.")
        paymentProducerPort.send(payment, StatusPayment.PENDING)
    }

    suspend fun processPayment(payment: Payment) {
        log.info("Requesting payment with correlation ID: ${payment.correlationId}, amount: ${payment.amount}, requested at: ${payment.requestedAt}")

        var statusPayment: StatusPayment

        val paymentType = validateService.canProcessPayment()

        if (checkPaymentProcessing(payment)) {
            log.info("Payment with correlation ID: ${payment.correlationId} is already being processed.")
            return
        }

        if (paymentType != null) {
            statusPayment = getStatusProcess(payment, paymentType)
        } else {
            log.warn("Payment processor is not available for correlation ID: ${payment.correlationId}")
            statusPayment = StatusPayment.PENDING
        }

        log.info("Payment status for correlation ID: ${payment.correlationId} is $statusPayment")
        paymentRepository.save(payment, statusPayment)

        if (statusPayment != StatusPayment.SUCCESS) {
            paymentProducerPort.send(payment, statusPayment)
        }
    }

    private suspend fun getStatusProcess(
        payment: Payment,
        paymentType: TypePayment
    ) = try {
        paymentProcessorService.callPaymentProcessor(payment, paymentType)
        StatusPayment.SUCCESS
    } catch (_: AlreadyProcessedRuntimeException) {
        log.info("Payment with correlation ID: ${payment.correlationId} already processed, skipping.")
        StatusPayment.SUCCESS
    } catch (_: TimeoutRuntimeException) {
        log.warn("Payment processor timed out for correlation ID: ${payment.correlationId}, falling back to fallback processor.")
        StatusPayment.TIMEOUT
    } catch (_: Exception) {
        log.error("Payment processor failed for correlation ID: ${payment.correlationId}, falling back to fallback processor.")
        StatusPayment.PENDING
    }

    suspend fun processPendingPayments(payment: Payment) {
        try {
            log.info("Processing pending payment with correlation ID: ${payment.correlationId}")

            validatePaymentProcessed(payment, StatusPayment.SUCCESS)

            processPayment(payment)
        } catch (ex: AlreadyProcessedRuntimeException) {
            log.debug("Payment with correlation ID: ${payment.correlationId} has already been processed, skipping.", ex)
        } catch (ex: Exception) {
            throw ex.also {
                log.error("Failed to process pending payment with correlation ID: ${payment.correlationId}", it)
            }
        }
    }

    suspend fun processTimeoutPayments(payment: Payment) {
        try {
            log.info("Processing timeout payment with correlation ID: ${payment.correlationId}")

            val response = paymentProcessorService.requestPaymentById(payment.correlationId)

            if (response == null) {
                processPayment(payment)
                return
            }
            paymentRepository.delete(payment.correlationId)
            paymentRepository.save(payment, StatusPayment.SUCCESS)
        } catch (ex: Exception) {
            log.error("Failed to process timeout payment with correlation ID: ${payment.correlationId}", ex)
        }
    }

    private fun checkPaymentProcessing(payment: Payment): Boolean {
        return paymentRepository.getAndSet(payment, StatusPayment.PROCESSING) != null
    }


    fun validatePaymentProcessed(payment: Payment, status: StatusPayment? = null) {
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