package com.jjeanjacques.rinhabackend.adapter.input.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.jjeanjacques.rinhabackend.domain.enums.StatusPayment
import com.jjeanjacques.rinhabackend.domain.enums.TypePayment
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentProducerPort
import com.jjeanjacques.rinhabackend.domain.service.PaymentService
import com.jjeanjacques.rinhabackend.domain.service.ValidateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class PaymentConsumerRedis(
    private val validateService: ValidateService,
    private val paymentService: PaymentService,
    private val paymentProducerPort: PaymentProducerPort,
    private val objectMapper: ObjectMapper
) : MessageListener {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessage(message: Message, pattern: ByteArray?) {
        CoroutineScope(Dispatchers.IO).launch {
            val payment = message.toPayment()

            val paymentType = validateService.canProcessPayment()
            if (paymentType == null) {
                log.warn("[${payment.correlationId}] Payment processor is not available, cannot process payments asynchronously.")
                paymentProducerPort.send(payment, payment.status)
                return@launch
            }

            log.info("[${payment.correlationId}] Processing payment: $payment")
            payment.type = paymentType
            val status = paymentService.processPayment(payment)
            if (status == StatusPayment.ERROR) {
                validateService.incrementErrorCount()
            }
        }
    }

    private fun Message.toPayment(): Payment {
        val json = body.decodeToString()
        val node = objectMapper.readTree(json)
        return Payment(
            correlationId = node["correlationId"].asText(),
            amount = BigDecimal(node["amount"].asText().toDouble()),
            requestedAt = Instant.parse(node["requestedAt"].asText()),
            workerId = node["workerId"].asText(),
            type = node["type"]?.asText()?.takeIf { it.isNotEmpty() }?.let { TypePayment.valueOf(it) } ?: TypePayment.DEFAULT,
            status = StatusPayment.valueOf(node["status"].asText())
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}