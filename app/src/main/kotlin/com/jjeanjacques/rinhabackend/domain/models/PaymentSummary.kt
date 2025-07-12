package com.jjeanjacques.rinhabackend.domain.models

import java.math.BigDecimal

data class PaymentSummary(
    val default: DefaultDetails,
    val fallback: FallbackDetails,
)

data class DefaultDetails(
    val totalRequests: Int,
    val totalAmount: BigDecimal
)

data class FallbackDetails(
    val totalRequests: Int,
    val totalAmount: BigDecimal
)