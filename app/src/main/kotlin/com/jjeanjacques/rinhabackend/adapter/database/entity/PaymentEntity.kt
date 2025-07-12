package com.jjeanjacques.rinhabackend.adapter.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "payments")
class PaymentEntity(
    @Id
    @Column(name = "correlationid")
    val correlationId: UUID,

    @Column(name = "amount", nullable = false)
    val amount: BigDecimal,

    @Column(name = "requested_at", nullable = false)
    var requestedAt: Instant
)
