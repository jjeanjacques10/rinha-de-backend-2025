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
        redisTemplate.opsForValue().set(key, status, 4900, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    override fun get(key: String): String? {
        return redisTemplate.opsForValue().get(key).also { value ->
            log.debug("Retrieved status for key: $key, value: $value")
        }
    }

    override fun getAndSet(key: String, value: String): String? {
        return redisTemplate.opsForValue().getAndSet(key, value)
            .also { it ->
                log.debug("Retrieved status for key: $key, value: $it and set new value: $value")
            }
    }

    override fun delete(key: String) {
        redisTemplate.delete(key)
        log.debug("Deleted key: $key from Redis")
    }

    override fun incrementErrorCount(key: String): Long {
        val currentCount = redisTemplate.opsForValue().increment(key, 1) ?: 0
        log.debug("Incremented error count for key: $key, new count: $currentCount")
        return currentCount
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}