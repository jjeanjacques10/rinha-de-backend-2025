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

    @Scheduled(fixedRate = 1000)
    suspend fun processPaymentAsync() {
        log.info("Validating payment processor status to process payments asynchronously")

        if (validateStatusPort.canProcessPayment() != null) {
            paymentService.processPendingPayments()
        } else {
            log.error("Payment processor is not available, cannot process payments asynchronously.")
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}