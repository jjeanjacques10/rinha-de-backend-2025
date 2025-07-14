package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.models.Payment
import java.util.UUID

interface PaymentRepository {
    fun save(payment: Payment)
    fun findById(id: String): Payment?
    fun getAll(): List<Payment>
    fun checkExists(correlationId: UUID): Boolean
}