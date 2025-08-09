package com.jjeanjacques.rinhabackend.adapter.input.cron

import com.jjeanjacques.rinhabackend.domain.service.ValidateService
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ValidateStatusScheduler(
    private val validateService: ValidateService,
    @Value("\${worker.id}") private val workerId: String
) {

    @Scheduled(fixedRate = 500)
    suspend fun scheduleValidateStatus() {
        if (workerId == "1") {
            log.debug("Starting scheduled validation of payment processor status")
            validateService.validatePaymentProcessorStatus()
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}