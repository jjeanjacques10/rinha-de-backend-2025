package com.jjeanjacques.rinhabackend.adapter.input.cron

import com.jjeanjacques.rinhabackend.domain.service.ValidateService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ValidateStatusScheduler(
    private val validateService: ValidateService
) {

    @Scheduled(fixedRate = 2000)
    suspend fun scheduleValidateStatus() {
        log.debug("Starting scheduled validation of payment processor status")
        validateService.validatePaymentProcessorStatus()
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}