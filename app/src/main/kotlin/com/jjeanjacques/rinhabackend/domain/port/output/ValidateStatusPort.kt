package com.jjeanjacques.rinhabackend.domain.port.output

import com.jjeanjacques.rinhabackend.domain.enums.TypePayment

interface ValidateStatusPort {
    fun save(key: String, status: String)
    fun get(key: String): String
    fun canProcessPayment(): TypePayment?

    companion object {
        const val API_PAYMENT_PROCESSOR_STATUS = "api.payment.processor.status"
        const val API_PAYMENT_PROCESSOR_FALLBACK_STATUS = "api.payment.processor.fallback.status"
    }
}