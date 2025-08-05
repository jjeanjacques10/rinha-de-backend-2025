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
            log.debug("Payment processor status is already validated as ${lastStatus}.")
            return
        } else log.debug("Validating payment processor status...")
        validateStatusPort.save(API_PAYMENT_PROCESSOR_STATUS, "updating")

        val status = paymentProcessorService.requestPaymentProcessorStatus(TypePayment.DEFAULT)
        val statusFallback = paymentProcessorService.requestPaymentProcessorStatus(TypePayment.FALLBACK)

        saveStatus(API_PAYMENT_PROCESSOR_STATUS, status)
        saveStatus(API_PAYMENT_PROCESSOR_FALLBACK_STATUS, statusFallback)

        if (status != null && statusFallback != null && status.failing && statusFallback.failing) {
            log.error("Both payment processors are failing. Default: ${status.failing}, Fallback: ${statusFallback.failing}")
        }
    }

    private fun saveStatus(key: String, status: PaymentProcessorStatusResponse?) {
        if (status == null) return
        validateStatusPort.save(key, status.minResponseTime.toString())
        when {
            status.failing -> validateStatusPort.save(key, "failing")
            else -> validateStatusPort.save(key, "ok")
        }
    }

    fun canProcessPayment(): TypePayment? {
        val defaultStatus = (validateStatusPort.get(API_PAYMENT_PROCESSOR_STATUS) ?: "ok") == "ok"
        val fallbackStatus = (validateStatusPort.get(API_PAYMENT_PROCESSOR_FALLBACK_STATUS) ?: "ok") == "ok"

        val timeoutStatus = (validateStatusPort.get(API_PAYMENT_PROCESSOR_STATUS)?.toIntOrNull() ?: 0)
        val timeoutStatusFallback = (validateStatusPort.get(API_PAYMENT_PROCESSOR_FALLBACK_STATUS)?.toIntOrNull() ?: 0)

        return when {
            !defaultStatus && !fallbackStatus -> null
            defaultStatus && timeoutStatus <= 1000 -> TypePayment.DEFAULT
            fallbackStatus && timeoutStatusFallback <= 1000 -> TypePayment.FALLBACK
            defaultStatus -> TypePayment.DEFAULT
            fallbackStatus -> TypePayment.FALLBACK
            else -> null
        }.also { type ->
            log.debug("Can process payment with type: {}", type)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }

}