package com.jjeanjacques.rinhabackend.adapter.output.rest.response

import java.math.BigDecimal
import java.time.Instant

data class PaymentDetailsResponse(
    val correlationId: String,
    val amount: BigDecimal,
    val requested_at: Instant
)