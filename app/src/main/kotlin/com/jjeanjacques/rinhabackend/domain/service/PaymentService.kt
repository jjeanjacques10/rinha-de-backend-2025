package com.jjeanjacques.rinhabackend.domain.service

import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {
    fun processPayment(request: Payment) {
        log.info("Processing payment with correlation ID: ${request.correlationId}, amount: ${request.amount}, requested at: ${request.requestedAt}")

        paymentRepository.savePayment(request)
    }

    fun getPayment(id: UUID): Payment {
        log.info("Retrieving payment with ID: $id")

        return paymentRepository.getPaymentById(id)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}