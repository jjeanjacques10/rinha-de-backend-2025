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

@Repository
class PaymentReposityRedis(
    private val redisTemplate: ReactiveRedisTemplate<String, PaymentProcessorRedis>
) : PaymentRepository {

    override suspend fun save(payment: Payment, status: StatusPayment) {
        val paymentProcessorRedis = PaymentProcessorRedis(
            correlationId = payment.correlationId,
            amount = payment.amount.toString(),
            type = payment.type.name,
            requestedAt = payment.requestedAt.toString(),
            status = status.name
        )

        redisTemplate.opsForZSet()
            .add(KEY_PAYMENT_BY_DATE, paymentProcessorRedis, payment.requestedAt?.toEpochMilli()!!.toDouble())
            .awaitSingle()

        log.info("[${payment.correlationId}] Saved payment [${status}], requested at: ${payment.requestedAt}")
    }

    override suspend fun getAndSet(
        payment: Payment,
        status: StatusPayment
    ): Payment? {
        var paymentProcessorRedis: PaymentProcessorRedis? = PaymentProcessorRedis(
            correlationId = payment.correlationId,
            amount = payment.amount.toString(),
            requestedAt = payment.requestedAt.toString(),
            type = payment.type.name,
            status = status.name,
            workerId = payment.workerId
        )
        paymentProcessorRedis = redisTemplate.opsForValue().getAndSet(payment.correlationId, paymentProcessorRedis!!).awaitSingleOrNull()
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
                log.info("[${payment.correlationId}] Updated payment, status: $status")
            }
            null
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


    override suspend fun checkExists(correlationId: String): Boolean {
        return redisTemplate.hasKey(correlationId).awaitFirstOrDefault(false).also {
            log.info("[$correlationId] Payment exists: $it")
        }
    }

    companion object {
        private const val KEY_PAYMENT_BY_DATE = "PAYMENTS_BY_DATE"

        private val log = LoggerFactory.getLogger(this::class.java)
    }
}