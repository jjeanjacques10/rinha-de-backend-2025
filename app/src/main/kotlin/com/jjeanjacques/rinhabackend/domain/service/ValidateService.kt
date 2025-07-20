package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.adapter.output.rest.PaymentProcessorService
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

        if (status.failing || status.minResponseTime >= 1000) {
            validateStatusPort.save(API_PAYMENT_PROCESSOR_STATUS, "failing")
            // May be configured a validation to response time
        } else {
            validateStatusPort.save(API_PAYMENT_PROCESSOR_STATUS, "ok")
        }

        if (statusFallback.failing) {
            validateStatusPort.save(API_PAYMENT_PROCESSOR_FALLBACK_STATUS, "failing")
        } else {
            validateStatusPort.save(API_PAYMENT_PROCESSOR_FALLBACK_STATUS, "ok")
        }

        log.info(
            "Payment processor status updated: " +
                    "Default status failing: ${status.failing}, " +
                    "Fallback status failing: ${statusFallback.failing}"
        )
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }

}