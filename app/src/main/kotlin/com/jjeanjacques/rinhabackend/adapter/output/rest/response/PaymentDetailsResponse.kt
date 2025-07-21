package com.jjeanjacques.rinhabackend.adapter.output.rest.response

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentDetailsResponse(
    val correlationId: UUID,
    val amount: BigDecimal,
    val requested_at: Instant
)