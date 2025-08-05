package com.jjeanjacques.rinhabackend.infra

import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig {

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic("paymentQueue")
    }

    @Bean
    fun redisTemplate(factory: RedisConnectionFactory): RedisTemplate<String, PaymentProcessorRedis> {
        val template = RedisTemplate<String, PaymentProcessorRedis>()
        val serializer = Jackson2JsonRedisSerializer(PaymentProcessorRedis::class.java)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = serializer
        template.setConnectionFactory(factory)
        return template
    }


    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String?, PaymentProcessorRedis?> {
        val serializer = Jackson2JsonRedisSerializer(PaymentProcessorRedis::class.java)
        val builder =
            RedisSerializationContext.newSerializationContext<String?, PaymentProcessorRedis?>(StringRedisSerializer())
        val context = builder.value(serializer).build()
        return ReactiveRedisTemplate<String?, PaymentProcessorRedis?>(factory, context)
    }

}
