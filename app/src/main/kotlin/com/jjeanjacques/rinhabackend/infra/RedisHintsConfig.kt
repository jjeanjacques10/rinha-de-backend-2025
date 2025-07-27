package com.jjeanjacques.rinhabackend.infra


import com.jjeanjacques.rinhabackend.adapter.input.consumer.PaymentConsumerRedis
import com.jjeanjacques.rinhabackend.adapter.output.redis.entity.PaymentProcessorRedis
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints


@Configuration
@ImportRuntimeHints(RedisHints::class)
class RedisHintsConfig

class RedisHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection().registerMethod(
            PaymentConsumerRedis::class.java.getMethod(
                "onMessage",
                org.springframework.data.redis.connection.Message::class.java,
                ByteArray::class.java
            ),
            ExecutableMode.INVOKE
        )

        hints.reflection().registerType(
            PaymentProcessorRedis::class.java,
            org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS
        )
    }
}