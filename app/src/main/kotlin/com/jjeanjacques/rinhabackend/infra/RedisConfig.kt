package com.jjeanjacques.rinhabackend.infra

import com.jjeanjacques.rinhabackend.adapter.input.redis.PaymentConsumerRedis
import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import com.jjeanjacques.rinhabackend.domain.port.output.ValidateStatusPort
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import com.jjeanjacques.rinhabackend.domain.service.ValidateService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

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
    fun topic(): ChannelTopic {
        return ChannelTopic("paymentQueue")
    }

    @Bean
    fun messageListener(
        validateService: ValidateService,
        paymentService: PaymentService,
        paymentProducerPort: PaymentProducerPort
    ): MessageListenerAdapter {
        return MessageListenerAdapter(PaymentConsumerRedis(validateService, paymentService, paymentProducerPort))
    }

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        messageListenerAdapter: MessageListenerAdapter,
        topic: ChannelTopic
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(messageListenerAdapter, topic)
        return container
    }
}
