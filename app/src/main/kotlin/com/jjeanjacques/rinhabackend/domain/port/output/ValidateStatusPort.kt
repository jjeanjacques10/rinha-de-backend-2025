package com.jjeanjacques.rinhabackend.domain.port.output

interface ValidateStatusPort {
    fun save(key: String, status: String)
    fun get(key: String): String?
    fun getAndSet(key: String, value: String): String?
    fun delete(key: String)
    fun incrementErrorCount(key: String): Long

    companion object {
        const val API_PAYMENT_PROCESSOR_STATUS = "processor.status"
        const val API_PAYMENT_PROCESSOR_FALLBACK_STATUS = "processor.fallback.status"
        const val API_ERROR_COUNT = "processor.error.count"
    }
}