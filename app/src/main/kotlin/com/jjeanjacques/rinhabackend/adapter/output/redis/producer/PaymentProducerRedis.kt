package com.jjeanjacques.rinhabackend.adapter.output.redis.producer

import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component

@Component
class PaymentProducerRedis(
    private val redisTemplate: ReactiveRedisTemplate<String, PaymentProcessorRedis>,
    private val topic: ChannelTopic,
    @Value("\${worker.id}")
    private val workerId: String
) : PaymentProducerPort {

    override suspend fun send(payment: Payment, statusPayment: StatusPayment) {
        val paymentProcessorRedis = PaymentProcessorRedis(
            correlationId = payment.correlationId,
            amount = payment.amount.toString(),
            requestedAt = payment.requestedAt.toString(),
            status = statusPayment.name,
            workerId = workerId
        )
        redisTemplate.convertAndSend(topic.topic, paymentProcessorRedis).awaitSingle()
        log.info("[${payment.correlationId}] Sent payment to Redis topic: ${topic.topic}, paymentRedis: $paymentProcessorRedis")
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}