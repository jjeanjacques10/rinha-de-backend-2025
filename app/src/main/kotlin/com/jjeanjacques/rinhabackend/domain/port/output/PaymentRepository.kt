package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import java.time.Instant
import java.util.UUID

interface PaymentRepository {
    fun save(payment: Payment, status: StatusPayment = StatusPayment.SUCCESS)
    fun getAndSet(payment: Payment, status: StatusPayment): Payment?
    fun delete(correlationId: UUID)
    fun findByDateRange(from: Instant, to: Instant): List<Payment>
    fun checkExists(correlationId: UUID): Boolean
}