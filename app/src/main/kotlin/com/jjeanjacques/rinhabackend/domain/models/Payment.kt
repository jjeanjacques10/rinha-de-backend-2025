package com.jjeanjacques.rinhabackend.domain.models

import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Payment(
    val correlationId: UUID,
    val amount: BigDecimal,
    var requestedAt: Instant? = Instant.now(),
    var type: TypePayment = TypePayment.DEFAULT,
    var workerId: String? = null
)