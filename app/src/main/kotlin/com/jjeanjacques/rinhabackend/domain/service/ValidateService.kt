package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.adapter.output.rest.PaymentProcessorService
import com.jjeanjacques.rinhabackend.adapter.output.rest.response.PaymentProcessorStatusResponse
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort
import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort.Companion.API_PAYMENT_PROCESSOR_FALLBACK_STATUS
import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort.Companion.API_PAYMENT_PROCESSOR_STATUS
import org.springframework.stereotype.Service

@Service
class ValidateService(
    val paymentProcessorService: PaymentProcessorService,
    val validateStatusPort: ValidateStatusPort
) {

    suspend fun validatePaymentProcessorStatus() {

        val lastStatus = validateStatusPort.get(API_PAYMENT_PROCESSOR_STATUS)

        if (lastStatus != null) {
            log.info("Payment processor status is already validated as ok.")
            return
        } else log.info("Validating payment processor status...")

        val status = paymentProcessorService.requestPaymentProcessorStatus(TypePayment.DEFAULT)
        val statusFallback = paymentProcessorService.requestPaymentProcessorStatus(TypePayment.FALLBACK)

        saveStatus(status, API_PAYMENT_PROCESSOR_STATUS)
        saveStatus(statusFallback, API_PAYMENT_PROCESSOR_FALLBACK_STATUS)

        if (status != null && statusFallback != null && status.failing && statusFallback.failing) {
            log.error("Both payment processors are failing. Default: ${status.failing}, Fallback: ${statusFallback.failing}")
        }
    }

    private fun saveStatus(status: PaymentProcessorStatusResponse?, key: String) {
        if (status == null) return
        when {
            status.failing -> validateStatusPort.save(key, "failing")
            status.minResponseTime >= 1000 -> validateStatusPort.save(key, "slow")
            else -> validateStatusPort.save(key, "ok")
        }
    }

    fun canProcessPayment(): TypePayment? {
        val defaultStatus = validateStatusPort.get(API_PAYMENT_PROCESSOR_STATUS) == "ok"
        val fallbackStatus = validateStatusPort.get(API_PAYMENT_PROCESSOR_FALLBACK_STATUS) == "ok"

        val timeoutStatus = validateStatusPort.get(API_PAYMENT_PROCESSOR_STATUS) == "slow"
        val timeoutStatusFallback = validateStatusPort.get(API_PAYMENT_PROCESSOR_FALLBACK_STATUS) == "slow"

        return when {
            defaultStatus && !timeoutStatus -> TypePayment.DEFAULT
            fallbackStatus && !timeoutStatusFallback -> TypePayment.FALLBACK
            else -> null
        }.also { type ->
            log.debug("Can process payment with type: {}", type)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }

}