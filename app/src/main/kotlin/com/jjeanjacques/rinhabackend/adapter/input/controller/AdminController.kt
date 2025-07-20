package com.jjeanjacques.rinhabackend.adapter.input.controller

import com.jjeanjacques.rinhabackend.domain.models.AdminPaymentSummary
import com.jjeanjacques.rinhabackend.domain.service.AdminService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService
) {

    @PostMapping("/purge-payments")
    suspend fun purgePayments(
        @RequestHeader("X-Rinha-Token") tokenRinha: String?
    ): Map<String, String> {
        adminService.deleteAllPayments()
        log.info("All payments purged (development only)")
        return mapOf("message" to "All payments purged.")
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}