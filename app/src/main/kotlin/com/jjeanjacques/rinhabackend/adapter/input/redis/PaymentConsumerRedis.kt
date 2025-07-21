package com.jjeanjacques.rinhabackend.adapter.input.redis

import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import com.jjeanjacques.rinhabackend.domain.service.ValidateService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Service
class PaymentConsumerRedis(
    val validateService: ValidateService,
    val paymentService: PaymentService,
    val paymentProducerPort: PaymentProducerPort
) : MessageListener {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val payment = message.toPayment()
        val status = getPaymentStatus(message)
        if (validateService.canProcessPayment() != null) {
            log.info("Processing payment asynchronously: ${payment.correlationId}, type: ${payment.type}, requested at: ${payment.requestedAt}")

            kotlinx.coroutines.GlobalScope.launch {
                when (status) {
                    StatusPayment.TIMEOUT -> {
                        delay(1000)
                        paymentService.processTimeoutPayments(payment)
                    }

                    else -> paymentService.processPendingPayments(payment)
                }
            }
        } else {
            log.error("Payment processor is not available, cannot process payments asynchronously.")
            paymentProducerPort.send(payment, status)
        }
    }

    private fun getPaymentStatus(message: Message): StatusPayment {
        val status = message.body.decodeToString()
            .let { extractJsonField(it, "status") }
            .let { StatusPayment.valueOf(it) }
        return status
    }

    private fun Message.toPayment() = Payment(
        correlationId = this.body.decodeToString()
            .let { extractJsonField(it, "correlationId") }.let { UUID.fromString(it) },
        amount = this.body.decodeToString().let { extractJsonField(it, "amount") }
            .let { BigDecimal.valueOf(it.toDouble()) },
        requestedAt = this.body.decodeToString().let { extractJsonField(it, "requestedAt") }
            .let { Instant.parse(it) },
        type = this.body.decodeToString().let { extractJsonField(it, "type") }.let { TypePayment.valueOf(it) }
    )


    private fun extractJsonField(json: String, field: String): String =
        json.substringAfter("\"$field\":\"").substringBefore("\"")

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}