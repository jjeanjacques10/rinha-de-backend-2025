package com.jjeanjacques.rinhabackend.adapter.output.redis.entity

data class PaymentProcessorRedis(
    var correlationId: String,
    var amount: String,
    var requestedAt: String,
    var type: String
)