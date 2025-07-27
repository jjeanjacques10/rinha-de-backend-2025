package com.jjeanjacques.rinhabackend.infra.warmup

import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
class WarmupIntegrations(
    private val redisTemplate: RedisTemplate<String, String>,
    private val ReactiveRedisRedisTemplate: ReactiveRedisTemplate<String, PaymentProcessorRedis>,
) : InitializingBean {

    override fun afterPropertiesSet() {
        log.info("Starting warmup for Redis integration...")
        try {
            // Warm up Redis by performing a simple operation
            redisTemplate.opsForValue().set("warmupKey", "warmupValue")
            redisTemplate.opsForValue().get("warmupKey")?.let {
                log.info("Redis warmup successful: $it")
            } ?: log.warn("Redis warmup failed, key not found")

            // Warm up Reactive Redis Template
            ReactiveRedisRedisTemplate.opsForValue().set("reactiveWarmupKey", PaymentProcessorRedis()).subscribe {
                log.info("Reactive Redis warmup successful")
            }
        } catch (e: Exception) {
            log.error("Error during Redis warmup", e)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(WarmupIntegrations::class.java)
    }
}