package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.models.Payment
import java.time.Instant

interface AdminPaymentRepository {
    fun getPaymentsByRange(from: Instant, to: Instant): List<Payment>
    fun deleteAll()
}