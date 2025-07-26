package com.jjeanjacques.rinhabackend.adapter.output.redis.producer

import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component

@Component
class PaymentProducerRedis(
    private val redisTemplate: RedisTemplate<String, PaymentProcessorRedis>,
    private val topic: ChannelTopic
) : PaymentProducerPort {

    override fun send(payment: Payment, statusPayment: StatusPayment) {
        val paymentProcessorRedis = PaymentProcessorRedis(
            correlationId = payment.correlationId.toString(),
            amount = payment.amount.toString(),
            requestedAt = payment.requestedAt.toString(),
            type = payment.type.name,
            status = statusPayment.name,
            workerId = payment.workerId!!
        )
        redisTemplate.convertAndSend(topic.topic, paymentProcessorRedis)
        log.info("Sent payment to Redis topic: ${topic.topic}, payment: $paymentProcessorRedis")
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}