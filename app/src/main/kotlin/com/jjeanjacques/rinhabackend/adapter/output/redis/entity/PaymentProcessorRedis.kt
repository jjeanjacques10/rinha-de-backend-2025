package com.jjeanjacques.rinhabackend.adapter.output.redis.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.redis.core.RedisHash

@RedisHash
data class PaymentProcessorRedis @JsonCreator constructor(
    @JsonProperty("correlationId") val correlationId: String = "",
    @JsonProperty("amount") val amount: String = "",
    @JsonProperty("requestedAt") val requestedAt: String = "",
    @JsonProperty("type") val type: String = "",
    @JsonProperty("status") val status: String = ""
)