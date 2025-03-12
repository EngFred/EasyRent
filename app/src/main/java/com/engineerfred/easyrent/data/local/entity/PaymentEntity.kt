package com.engineerfred.easyrent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("payments")
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    val amount: Float,
    val newBalance: Float,
    val paymentDate: Long = System.currentTimeMillis(),
    val tenantId: String,
    val tenantName: String,
    val roomNumber: Int,
    val paymentMethod: String,
    val isSynced: Boolean,
    val isDeleted: Boolean = false,
    val userId: String
)
