package com.jjeanjacques.rinhabackend.infra

import com.jjeanjacques.rinhabackend.adapter.input.consumer.PaymentConsumerRedis
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import com.jjeanjacques.rinhabackend.domain.service.ValidateService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter


@Profile("worker")
@Configuration
class RedisConsumerConfig {

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
