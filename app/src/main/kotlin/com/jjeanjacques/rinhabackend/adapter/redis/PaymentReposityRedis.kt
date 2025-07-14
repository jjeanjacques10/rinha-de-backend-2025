package com.jjeanjacques.rinhabackend.adapter.redis

import com.jjeanjacques.rinhabackend.adapter.redis.entity.PaymentProcessorRedis
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class PaymentReposityRedis(
    private val redisTemplate: RedisTemplate<String, PaymentProcessorRedis>
) : PaymentRepository {

    override fun save(payment: Payment) {
        val paymentProcessorRedis = PaymentProcessorRedis(
            correlationId = payment.correlationId.toString(),
            amount = payment.amount.toString(),
            requestedAt = payment.requestedAt.toString(),
            type = payment.type.name
        )
        val key = payment.correlationId.toString()
        redisTemplate.opsForValue().set(key, paymentProcessorRedis)
        // Adiciona ao Sorted Set
        redisTemplate.opsForZSet()
            .add(paymentsByDateKey, paymentProcessorRedis, payment.requestedAt?.epochSecond!!.toDouble())
        log.info("Saved payment with correlation ID: ${payment.correlationId}, type: ${payment.type}, requested at: ${payment.requestedAt}")
    }

    override fun findByDateRange(from: Instant, to: Instant): List<Payment> {
        return redisTemplate.opsForZSet()
            .rangeByScore(paymentsByDateKey, from.epochSecond.toDouble(), to.epochSecond.toDouble())
            ?.mapNotNull { paymentProcessorRedis ->
                Payment(
                    correlationId = UUID.fromString(paymentProcessorRedis.correlationId),
                    amount = paymentProcessorRedis.amount.toBigDecimal(),
                    requestedAt = Instant.parse(paymentProcessorRedis.requestedAt),
                    type = TypePayment.valueOf(paymentProcessorRedis.type)
                )
            }!!
    }

    override fun checkExists(correlationId: UUID): Boolean {
        return redisTemplate.opsForValue().get(correlationId.toString()) != null
            .also { exists ->
                log.info("Payment with correlation ID: $correlationId exists: $exists")
            }
    }

    companion object {
        private val paymentsByDateKey = "PAYMENTS_BY_DATE"

        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}