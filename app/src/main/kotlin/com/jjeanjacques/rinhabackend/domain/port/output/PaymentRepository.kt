package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import java.time.Instant
import java.util.UUID

interface PaymentRepository {
    fun save(payment: Payment, status: StatusPayment = StatusPayment.SUCCESS)
    fun delete(correlationId: UUID, status: StatusPayment)
    fun findByDateRange(from: Instant, to: Instant): List<Payment>
    fun checkExists(correlationId: UUID, status: StatusPayment? = null): Boolean
    fun getPaymentsByStatus(status: StatusPayment): List<Payment>
}