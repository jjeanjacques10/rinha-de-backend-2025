package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import java.time.Instant

interface PaymentRepository {
    suspend fun save(payment: Payment, status: StatusPayment = StatusPayment.SUCCESS)
    suspend fun getAndSet(payment: Payment, status: StatusPayment): Payment?
    suspend fun findByDateRange(from: Instant, to: Instant): List<Payment>
    suspend fun checkExists(correlationId: String): Boolean
}