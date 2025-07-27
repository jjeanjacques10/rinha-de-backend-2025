package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.models.Payment

interface PaymentProducerPort {
    suspend fun send(payment: Payment, statusPayment: StatusPayment)
}