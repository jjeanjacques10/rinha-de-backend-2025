package com.jjeanjacques.rinhabackend.adapter.output.rest.request

import java.math.BigDecimal

data class PaymentProcessorRequest(
    val correlationId: String,
    val amount: BigDecimal,
    var requestedAt: String
)