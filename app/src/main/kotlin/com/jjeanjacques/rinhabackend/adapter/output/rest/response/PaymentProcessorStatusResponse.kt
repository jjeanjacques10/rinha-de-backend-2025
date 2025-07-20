package com.jjeanjacques.rinhabackend.adapter.output.rest.response

data class PaymentProcessorStatusResponse(
    val failing: Boolean,
    val minResponseTime: Long
)
