package com.jjeanjacques.rinhabackend.adapter.output.database

import com.jjeanjacques.rinhabackend.domain.port.output.AdminPaymentRepository
import org.springframework.stereotype.Component

@Component
class AdminPaymentDatabaseAdapter(
    private val adminPaymentRepositoryAdapter: AdminPaymentRepositoryAdapter
) : AdminPaymentRepository {

    override fun deleteAll() {
        adminPaymentRepositoryAdapter.deleteAll()
    }
}