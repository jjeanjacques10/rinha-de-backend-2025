package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.domain.models.AdminPaymentSummary
import com.jjeanjacques.rinhabackend.domain.port.output.AdminPaymentRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class AdminService(
    private val adminPaymentRepository: AdminPaymentRepository
) {
    fun getSummary(from: String, to: String): AdminPaymentSummary {
        val payments = adminPaymentRepository.getPaymentsByRange(
            Instant.parse(if (from.endsWith("Z")) from else "${from}Z"),
            Instant.parse(if (to.endsWith("Z")) to else "${to}Z")
        )

        return AdminPaymentSummary(
            totalRequests = payments.size,
            totalAmount = payments.sumOf { it.amount },
            totalFee = BigDecimal.ZERO, // TODO: calculate later
            feePerTransaction = BigDecimal.ZERO // TODO: calculate later
        )
    }

    fun deleteAllPayments() {
        adminPaymentRepository.deleteAll()
    }
}