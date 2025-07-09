package com.jjeanjacques.rinhabackend.domain.models

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Payment(
    val correlationId: UUID,
    val amount: BigDecimal,
    val requestedAt: Instant
)