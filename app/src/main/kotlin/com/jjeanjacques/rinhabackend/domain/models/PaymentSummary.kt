package com.jjeanjacques.rinhabackend.domain.models

import java.math.BigDecimal

data class PaymentSummary(
    var totalRequests: Int,
    var totalAmount: BigDecimal,
    var totalFee: BigDecimal,
    var feePerTransaction: BigDecimal
)
