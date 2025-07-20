package com.jjeanjacques.rinhabackend.adapter.input.controller.response

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentDetailsResponse(
    val correlationId: UUID,
    val amount: BigDecimal,
    val requested_at: Instant
)