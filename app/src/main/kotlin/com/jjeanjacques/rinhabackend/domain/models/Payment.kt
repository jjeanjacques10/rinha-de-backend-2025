package com.jjeanjacques.rinhabackend.domain.models

import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import java.math.BigDecimal
import java.time.Instant

data class Payment(
    val correlationId: String,
    val amount: BigDecimal,
    var requestedAt: Instant? = Instant.now(),
    var type: TypePayment = TypePayment.DEFAULT,
    var workerId: String = "",
    var status: StatusPayment = StatusPayment.PENDING
)