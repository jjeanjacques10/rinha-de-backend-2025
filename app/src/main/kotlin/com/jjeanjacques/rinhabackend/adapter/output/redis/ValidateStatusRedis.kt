package com.jjeanjacques.rinhabackend.adapter.output.redis

import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort
import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort.Companion.API_PAYMENT_PROCESSOR_FALLBACK_STATUS
import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort.Companion.API_PAYMENT_PROCESSOR_STATUS
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class ValidateStatusRedis(
    private val redisTemplate: RedisTemplate<String, String>
) : ValidateStatusPort {

    override fun save(key: String, status: String) {
        redisTemplate.opsForValue().set(key, status)
    }

    override fun get(key: String): String {
        return redisTemplate.opsForValue().get(key)
            .also { value ->
                log.info("Retrieved status for key: $key, value: $value")
            } ?: "ok"
    }

    override fun canProcessPayment(): TypePayment? {
        val defaultStatus = get(API_PAYMENT_PROCESSOR_STATUS) == "ok"
        val fallbackStatus = get(API_PAYMENT_PROCESSOR_FALLBACK_STATUS) == "ok"

        val timeoutStatus = get(API_PAYMENT_PROCESSOR_STATUS) == "slow" ||
                get(API_PAYMENT_PROCESSOR_FALLBACK_STATUS) == "slow"

        return when {
            defaultStatus -> TypePayment.DEFAULT
            fallbackStatus -> TypePayment.FALLBACK
            timeoutStatus -> TypePayment.TIMEOUT
            else -> null
        }.also { type ->
            log.info("Can process payment with type: $type")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}