package com.jjeanjacques.rinhabackend.adapter.input.cron

import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ProcessPaymentAsync(
    val validateStatusPort: ValidateStatusPort,
    val paymentService: PaymentService
) {

    @Scheduled(fixedRate = 100)
    suspend fun processPaymentAsync() {
        if (validateStatusPort.canProcessPayment() != null) {
            paymentService.processPendingPayments()
        } else {
            log.error("Payment processor is not available, cannot process payments asynchronously.")
        }
    }

    @Scheduled(fixedRate = 100)
    suspend fun processTimeoutPayments() {
        if (validateStatusPort.canProcessPayment() != null) {
            paymentService.processTimeoutPayments()
        } else {
            log.error("Payment processor is not available, cannot process timeout payments.")
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}