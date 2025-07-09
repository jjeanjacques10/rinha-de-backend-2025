package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.models.Payment
import java.time.Instant
import java.util.*

interface PaymentRepository {
    fun savePayment(paymentRequest: Payment)
    fun getPaymentById(id: UUID): Payment
    fun getPaymentsByRange(from: Instant, to: Instant): List<Payment>
}