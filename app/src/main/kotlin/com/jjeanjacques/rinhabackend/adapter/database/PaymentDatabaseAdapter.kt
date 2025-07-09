package com.jjeanjacques.rinhabackend.adapter.database

import com.jjeanjacques.rinhabackend.adapter.database.entity.PaymentEntity
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class PaymentDatabaseAdapter(
    private val paymentRepositoryAdapter: PaymentRepositoryAdapter
) : PaymentRepository {

    override fun savePayment(paymentRequest: Payment) {
        paymentRepositoryAdapter.save(paymentRequest.let {
            PaymentEntity(
                correlationId = it.correlationId,
                amount = it.amount,
                requestedAt = it.requestedAt
            )
        })
    }

    override fun getPaymentById(id: UUID): Payment {
        val paymentEntity = paymentRepositoryAdapter.findById(id)
            .orElseThrow { IllegalArgumentException("Payment with ID $id not found") }

        return Payment(
            correlationId = paymentEntity.correlationId,
            amount = paymentEntity.amount,
            requestedAt = paymentEntity.requestedAt
        )
    }

    override fun getPaymentsByRange(from: Instant, to: Instant): List<Payment> {
        return paymentRepositoryAdapter.getPaymentsByRangeDate(from, to).map {
            Payment(
                correlationId = it.correlationId,
                amount = it.amount,
                requestedAt = it.requestedAt
            )
        }
    }
}