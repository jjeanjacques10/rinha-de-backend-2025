package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.domain.port.output.AdminPaymentRepository
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val adminPaymentRepository: AdminPaymentRepository
) {
    fun deleteAllPayments() {
        adminPaymentRepository.deleteAll()
    }
}