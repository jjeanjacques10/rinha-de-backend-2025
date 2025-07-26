package com.jjeanjacques.rinhabackend.infra.warmup

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
class WarmupIntegrations(
    private val redisTemplate: StringRedisTemplate
) : InitializingBean {

    override fun afterPropertiesSet() {
        log.info("Starting warmup for Redis integration...")
         redisTemplate.opsForValue().get("warmup-key")
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(WarmupIntegrations::class.java)
    }
}