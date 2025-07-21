package com.jjeanjacques.rinhabackend.adapter.output.redis

import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class ValidateStatusRedis(
    private val redisTemplate: RedisTemplate<String, String>
) : ValidateStatusPort {

    override fun save(key: String, status: String) {
        redisTemplate.opsForValue().set(key, status, 5, java.util.concurrent.TimeUnit.SECONDS)
    }

    override fun get(key: String): String? {
        return redisTemplate.opsForValue().get(key)
            .also { value ->
                log.debug("Retrieved status for key: $key, value: $value")
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}