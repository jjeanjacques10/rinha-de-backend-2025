package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.domain.models.PaymentSummary
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class AdminService(
    private val paymentRepository: PaymentRepository
) {
    fun getSummary(from: String, to: String): PaymentSummary {
        val payments = paymentRepository.getPaymentsByRange(Instant.parse(from), Instant.parse(to))

        return PaymentSummary(
            totalRequests = payments.size,
            totalAmount = payments.sumOf { it.amount },
            totalFee = BigDecimal.ZERO, // TODO: calculate later
            feePerTransaction = BigDecimal.ZERO // TODO: calculate later
        )
    }
}