package com.jjeanjacques.rinhabackend.adapter.output.rest.request

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentProcessorRequest(
    val correlationId: UUID,
    val amount: BigDecimal,
    var requestedAt: String
)