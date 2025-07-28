package com.jjeanjacques.rinhabackend.adapter.output.redis

import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class PaymentReposityRedis(
    private val redisTemplate: ReactiveRedisTemplate<String, PaymentProcessorRedis>
) : PaymentRepository {

    override suspend fun save(payment: Payment, status: StatusPayment) {
        val paymentProcessorRedis = PaymentProcessorRedis(
            correlationId = payment.correlationId.toString(),
            amount = payment.amount.toString(),
            type = payment.type.name,
            requestedAt = payment.requestedAt.toString(),
            status = status.name
        )

        redisTemplate.opsForZSet()
            .add(KEY_PAYMENT_BY_DATE, paymentProcessorRedis, payment.requestedAt?.toEpochMilli()!!.toDouble())
            .awaitSingle()

        log.info("Saved payment [${status}] with correlation ID: ${payment.correlationId}, requested at: ${payment.requestedAt}")
    }

    override suspend fun getAndSet(
        payment: Payment,
        status: StatusPayment
    ): Payment? {
        val key = "${payment.correlationId}"
        var paymentProcessorRedis: PaymentProcessorRedis? = PaymentProcessorRedis(
            correlationId = payment.correlationId.toString(),
            amount = payment.amount.toString(),
            requestedAt = payment.requestedAt.toString(),
            type = payment.type.name,
            status = status.name,
            workerId = payment.workerId
        )
        paymentProcessorRedis = redisTemplate.opsForValue().getAndSet(key, paymentProcessorRedis!!).awaitSingleOrNull()
        return paymentProcessorRedis?.let {
            Payment(
                correlationId = it.correlationId,
                amount = it.amount.toBigDecimal(),
                requestedAt = Instant.parse(it.requestedAt),
                type = TypePayment.valueOf(it.type),
                status = StatusPayment.valueOf(it.status),
                workerId = it.workerId
            )
        }.also { payment ->
            if (payment != null) {
                log.info("Updated payment with correlation ID: ${payment.correlationId}, status: $status")
            }
            null
        }

    }

    override suspend fun delete(correlationId: UUID) {
        redisTemplate.delete("$correlationId").awaitSingle().also {
            log.info("Deleted payment with correlation ID: $correlationId, exists: $it")
        }
    }

    override suspend fun findByDateRange(from: Instant, to: Instant): List<Payment> {
        val rangeDouble: Range<Double> = Range.closed(
            from.toEpochMilli().toDouble(),
            to.toEpochMilli().toDouble()
        )
        return redisTemplate.opsForZSet()
            .rangeByScore(KEY_PAYMENT_BY_DATE, rangeDouble)
            .collectList()
            .awaitFirstOrNull()
            ?.mapNotNull { paymentProcessorRedis ->
                Payment(
                    correlationId = paymentProcessorRedis.correlationId,
                    amount = paymentProcessorRedis.amount.toBigDecimal(),
                    requestedAt = Instant.parse(paymentProcessorRedis.requestedAt),
                    type = TypePayment.valueOf(paymentProcessorRedis.type),
                    status = StatusPayment.valueOf(paymentProcessorRedis.status),
                    workerId = paymentProcessorRedis.workerId
                )
            } ?: emptyList<Payment>().also { log.info("No payments found in the date range from $from to $to") }
    }


    override suspend fun checkExists(correlationId: UUID): Boolean {
        return redisTemplate.hasKey("$correlationId").awaitFirstOrDefault(false).also {
            log.info("Payment with correlation ID: $correlationId exists: $it")
        }
    }

    companion object {
        private const val KEY_PAYMENT_BY_DATE = "PAYMENTS_BY_DATE"

        private val log = LoggerFactory.getLogger(this::class.java)
    }
}