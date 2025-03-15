package com.engineerfred.easyrent.domain.repository

import com.engineerfred.easyrent.data.resource.Resource
import com.engineerfred.easyrent.domain.modals.Payment
import kotlinx.coroutines.flow.Flow

interface PaymentsRepository {
    fun getAllPayments() : Flow<Resource<List<Payment>>>
    fun getPaymentsByTenant(tenantId: String) : Flow<Resource<List<Payment>>>
    suspend fun getUnsyncedPayments() : List<Payment>?
    suspend fun getAllTrashedPayments() : Resource<List<Payment>>
    suspend fun insertPayment(payment: Payment) : Resource<Unit>
    suspend fun deletePayment(payment: Payment) : Resource<Unit>
}