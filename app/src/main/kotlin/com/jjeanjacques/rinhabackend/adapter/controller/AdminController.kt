package com.jjeanjacques.rinhabackend.adapter.controller

import com.jjeanjacques.rinhabackend.domain.models.AdminPaymentSummary
import com.jjeanjacques.rinhabackend.domain.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("/payments-summary")
    fun summaryPayments(
        @RequestParam(required = false) from: String,
        @RequestParam(required = false) to: String,
        @RequestHeader("X-Rinha-Token") tokenRinha: String?
    ): ResponseEntity<AdminPaymentSummary> {
        val summary = adminService.getSummary(from, to)
        return ResponseEntity.ok(summary)
    }

    @PostMapping("/purge-payments")
    fun purgePayments(
        @RequestHeader("X-Rinha-Token") tokenRinha: String?
    ): ResponseEntity<Map<String, String>> {
        adminService.deleteAllPayments()
        log.info("All payments purged (development only)")
        return ResponseEntity.ok()
            .body(mapOf("message" to "All payments purged."))
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}