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
        val status = paymentProcessorService.requestPaymentProcessorStatus(TypePayment.DEFAULT)
        val statusFallback = paymentProcessorService.requestPaymentProcessorStatus(TypePayment.FALLBACK)

        saveStatus(status, API_PAYMENT_PROCESSOR_STATUS)
        saveStatus(statusFallback, API_PAYMENT_PROCESSOR_FALLBACK_STATUS)

        if (status.failing && statusFallback.failing) {
            log.warn("Both payment processors are failing. Default: ${status.failing}, Fallback: ${statusFallback.failing}")
        }
    }

    private fun saveStatus(status: PaymentProcessorStatusResponse, key: String) {
        when {
            status.failing -> validateStatusPort.save(key, "failing")
            status.minResponseTime >= 500 -> validateStatusPort.save(key, "slow")
            else -> validateStatusPort.save(key, "ok")
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }

}