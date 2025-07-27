package com.jjeanjacques.rinhabackend.infra

import com.jjeanjacques.rinhabackend.adapter.input.consumer.PaymentConsumerRedis
import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import com.jjeanjacques.rinhabackend.domain.service.ValidateService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
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
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String?, PaymentProcessorRedis?> {
        val serializer = Jackson2JsonRedisSerializer(PaymentProcessorRedis::class.java)
        val builder =
            RedisSerializationContext.newSerializationContext<String?, PaymentProcessorRedis?>(StringRedisSerializer())
        val context = builder.value(serializer).build()
        return ReactiveRedisTemplate<String?, PaymentProcessorRedis?>(factory, context)
    }

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic("paymentQueue")
    }

    @Bean
    fun messageListener(
        validateService: ValidateService,
        paymentService: PaymentService,
        paymentProducerPort: PaymentProducerPort,
        @Value("\${worker.id}")
        workerId: String
    ): MessageListenerAdapter {
        return MessageListenerAdapter(
            PaymentConsumerRedis(
                validateService,
                paymentService,
                paymentProducerPort,
                workerId
            )
        )
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
