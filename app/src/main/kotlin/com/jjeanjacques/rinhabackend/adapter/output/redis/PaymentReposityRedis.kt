package com.jjeanjacques.rinhabackend.adapter.output.redis

import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class PaymentReposityRedis(
    private val redisTemplate: RedisTemplate<String, PaymentProcessorRedis>
) : PaymentRepository {

    override fun save(payment: Payment, status: StatusPayment) {
        val paymentProcessorRedis = PaymentProcessorRedis(
            correlationId = payment.correlationId.toString(),
            amount = payment.amount.toString(),
            requestedAt = payment.requestedAt.toString(),
            type = payment.type.name
        )
        val key = payment.correlationId.toString() + "#" + status.name
        redisTemplate.opsForValue().set(key, paymentProcessorRedis)
        // Adiciona ao Sorted Set
        if (status == StatusPayment.SUCCESS) {
            redisTemplate.opsForZSet()
                .add(paymentsByDateKey, paymentProcessorRedis, payment.requestedAt?.epochSecond!!.toDouble())
        }
        log.info("Saved payment [${status}] with correlation ID: ${payment.correlationId}, type: ${payment.type}, requested at: ${payment.requestedAt}")
    }

    override fun delete(correlationId: UUID, status: StatusPayment) {
        val keyPrefix = "$correlationId#"
        val keys = redisTemplate.keys("$keyPrefix*")
        if (keys != null && keys.isNotEmpty()) {
            redisTemplate.delete(keys)
            log.info("Deleted payment with correlation ID: $correlationId")
        } else {
            log.error("No payment found with correlation ID: $correlationId")
        }
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

    override fun checkExists(correlationId: UUID, status: StatusPayment?): Boolean {
        val exists = if (status != null) {
            redisTemplate.hasKey("$correlationId#${status.name}").also {
                log.info("Payment with correlation ID: $correlationId and status: $status exists: $it")
            }
        } else {
            redisTemplate.keys("${correlationId}#*").isNotEmpty().also {
                log.info("Payment with correlation ID prefix: $correlationId exists: $it")
            }
        }
        return exists
    }

    override fun getPendingPayments(): List<Payment> {
        return redisTemplate.keys("*#${StatusPayment.PENDING.name}")
            .mapNotNull { key ->
                redisTemplate.opsForValue().get(key)
            }.map { paymentProcessorRedis ->
                Payment(
                    correlationId = UUID.fromString(paymentProcessorRedis.correlationId),
                    amount = paymentProcessorRedis.amount.toBigDecimal(),
                    requestedAt = Instant.parse(paymentProcessorRedis.requestedAt),
                    type = TypePayment.valueOf(paymentProcessorRedis.type)
                )
            }
    }

    companion object {
        private val paymentsByDateKey = "PAYMENTS_BY_DATE"

        private val log = LoggerFactory.getLogger(this::class.java)
    }
}