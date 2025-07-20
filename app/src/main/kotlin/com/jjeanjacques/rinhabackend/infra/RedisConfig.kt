package com.jjeanjacques.rinhabackend.infra

import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, PaymentProcessorRedis> {
        val template = RedisTemplate<String, PaymentProcessorRedis>()
        template.setConnectionFactory(connectionFactory)
        template.keySerializer = StringRedisSerializer()
        val serializer =
            Jackson2JsonRedisSerializer(ObjectMapperConfig().objectMapper(), PaymentProcessorRedis::class.java)

        template.valueSerializer = serializer
        return template
    }
}

