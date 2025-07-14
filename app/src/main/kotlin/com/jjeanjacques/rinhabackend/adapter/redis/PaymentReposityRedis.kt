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
        redisTemplate.opsForValue().set(payment.correlationId.toString(), paymentProcessorRedis)
        log.info("Saved payment with correlation ID: ${payment.correlationId}, type: ${payment.type}, requested at: ${payment.requestedAt}")
    }

    override fun findById(id: String): Payment? {
        return redisTemplate.opsForValue().get(id).let {
            it?.let { paymentProcessorRedis ->
                Payment(
                    correlationId = UUID.fromString(paymentProcessorRedis.correlationId),
                    amount = paymentProcessorRedis.amount.toBigDecimal(),
                    requestedAt = Instant.parse(it.requestedAt),
                    type = TypePayment.valueOf(paymentProcessorRedis.type)
                )
            }
        }
    }

    override fun getAll(): List<Payment> {
        return redisTemplate.keys("*")
            .mapNotNull { findById(it) }
    }

    override fun checkExists(correlationId: UUID): Boolean {
        return redisTemplate.opsForValue().get(correlationId.toString()) != null
            .also { exists ->
                log.info("Payment with correlation ID: $correlationId exists: $exists")
            }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}