package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.models.Payment

interface PaymentRepository {
    fun save(payment: Payment)
    fun findById(id: String): Payment?
    fun getAll(): List<Payment>
}