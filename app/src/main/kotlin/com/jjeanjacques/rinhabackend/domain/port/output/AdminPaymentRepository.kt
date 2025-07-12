package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.models.Payment
import java.time.Instant
import java.util.*

interface AdminPaymentRepository {
    fun getPaymentById(id: UUID): Payment
    fun getPaymentsByRange(from: Instant, to: Instant): List<Payment>
    fun deleteAll()
}