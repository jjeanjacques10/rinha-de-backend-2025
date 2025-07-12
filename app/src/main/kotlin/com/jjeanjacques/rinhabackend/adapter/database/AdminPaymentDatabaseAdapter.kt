package com.jjeanjacques.rinhabackend.adapter.database

import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.AdminPaymentRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class AdminPaymentDatabaseAdapter(
    private val adminPaymentRepositoryAdapter: AdminPaymentRepositoryAdapter
) : AdminPaymentRepository {

    override fun getPaymentById(id: UUID): Payment {
        val paymentEntity = adminPaymentRepositoryAdapter.findById(id)
            .orElseThrow { IllegalArgumentException("Payment with ID $id not found") }

        return Payment(
            correlationId = paymentEntity.correlationId,
            amount = paymentEntity.amount,
            requestedAt = paymentEntity.requestedAt
        )
    }

    override fun getPaymentsByRange(from: Instant, to: Instant): List<Payment> {
        return adminPaymentRepositoryAdapter.getPaymentsByRangeDate(from, to).map {
            Payment(
                correlationId = it.correlationId,
                amount = it.amount,
                requestedAt = it.requestedAt
            )
        }
    }

    override fun deleteAll() {
        adminPaymentRepositoryAdapter.deleteAll()
    }
}