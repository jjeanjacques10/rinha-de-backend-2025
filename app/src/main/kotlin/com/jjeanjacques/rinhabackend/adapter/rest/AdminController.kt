package com.jjeanjacques.rinhabackend.adapter.rest

import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("/payments-summary")
    fun summaryPayments(
        @RequestParam(required = false) from: String,
        @RequestParam(required = false) to: String
    ): ResponseEntity<PaymentSummary> {
        val summary = adminService.getSummary(from, to)

        return ResponseEntity.ok(summary)
    }

}