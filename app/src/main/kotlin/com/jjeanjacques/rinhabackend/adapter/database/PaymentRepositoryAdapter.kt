package com.jjeanjacques.rinhabackend.adapter.database

import com.jjeanjacques.rinhabackend.adapter.database.entity.PaymentEntity
import com.jjeanjacques.rinhabackend.domain.models.Payment
import com.jjeanjacques.rinhabackend.domain.port.output.PaymentRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface PaymentRepositoryAdapter : JpaRepository<PaymentEntity, UUID> {

    @Query("SELECT p FROM PaymentEntity p WHERE p.requestedAt BETWEEN :from AND :to")
    fun getPaymentsByRangeDate(
        from: Instant,
        to: Instant
    ): List<PaymentEntity>
}