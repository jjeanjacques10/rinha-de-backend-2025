package com.jjeanjacques.rinhabackend.adapter.input.redis

import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import com.jjeanjacques.rinhabackend.domain.service.ValidateService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Service
class PaymentConsumerRedis(
    private val validateService: ValidateService,
    private val paymentService: PaymentService,
    private val paymentProducerPort: PaymentProducerPort,
    @Value("\${worker.id}")
    private val workerId: String
) : MessageListener {


    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessage(message: Message, pattern: ByteArray?) {
        kotlinx.coroutines.GlobalScope.launch {
            val payment = message.toPayment()

            if (payment.workerId != workerId) {
                log.warn("Received payment with worker ID: ${payment.workerId}, but current worker ID is: $workerId. Ignoring message.")
                return@launch
            }

            val status = getPaymentStatus(message)
            val paymentType = validateService.canProcessPayment()

            if (paymentType != null) {
                log.debug("Processing payment asynchronously: ${payment.correlationId}, type: ${payment.type}, requested at: ${payment.requestedAt}")

                when (status) {
                    StatusPayment.TIMEOUT -> {
                        paymentService.processTimeoutPayments(payment, paymentType)
                    }

                    else -> paymentService.processPendingPayments(payment, paymentType)
                }

            } else {
                log.error("Payment processor is not available, cannot process payments asynchronously.")
                paymentProducerPort.send(payment, status)
            }
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
        type = this.body.decodeToString().let { extractJsonField(it, "type") }.let { TypePayment.valueOf(it) },
        workerId = this.body.decodeToString().let { extractJsonField(it, "workerId") }
    )


    private fun extractJsonField(json: String, field: String): String =
        json.substringAfter("\"$field\":\"").substringBefore("\"")

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}