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
            type = payment.type.name,
            status = status.name
        )
        val key = payment.correlationId.toString()
        redisTemplate.opsForValue().set(key, paymentProcessorRedis)
        // Adiciona ao Sorted Set
        if (status == StatusPayment.SUCCESS) {
            redisTemplate.opsForZSet()
                .add(paymentsByDateKey, paymentProcessorRedis, payment.requestedAt?.epochSecond!!.toDouble())
        }
        log.info("Saved payment [${status}] with correlation ID: ${payment.correlationId}, type: ${payment.type}, requested at: ${payment.requestedAt}")
    }

    override fun getAndSet(
        payment: Payment,
        status: StatusPayment
    ): Payment? {
        val key = "${payment.correlationId}"
        var paymentProcessorRedis: PaymentProcessorRedis? = PaymentProcessorRedis(
            correlationId = payment.correlationId.toString(),
            amount = payment.amount.toString(),
            requestedAt = payment.requestedAt.toString(),
            type = payment.type.name,
            status = status.name
        )
        paymentProcessorRedis = redisTemplate.opsForValue().getAndSet(key, paymentProcessorRedis!!)
        return paymentProcessorRedis?.let {
            Payment(
                correlationId = UUID.fromString(it.correlationId),
                amount = it.amount.toBigDecimal(),
                requestedAt = Instant.parse(it.requestedAt),
                type = TypePayment.valueOf(it.type),
                workerId = it.workerId
            )
        }.also { payment ->
            if (payment != null) {
                log.info("Updated payment with correlation ID: ${payment.correlationId}, status: $status")
            }
            null
        }

    }

    override fun delete(correlationId: UUID) {
        redisTemplate.delete("$correlationId").also {
            log.info("Deleted payment with correlation ID: $correlationId, exists: $it")
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
                    type = TypePayment.valueOf(paymentProcessorRedis.type),
                    workerId = paymentProcessorRedis.workerId
                )
            }!!
    }

    override fun checkExists(correlationId: UUID): Boolean {
        return redisTemplate.hasKey("$correlationId").also {
            log.info("Payment with correlation ID: $correlationId exists: $it")
        }
    }

    companion object {
        private val paymentsByDateKey = "PAYMENTS_BY_DATE"

        private val log = LoggerFactory.getLogger(this::class.java)
    }
}