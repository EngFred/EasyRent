package com.engineerfred.easyrent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDto(
    val id: String,
    val amount: Float,
    @SerialName("new_balance")
    val newBalance: Float,
    @SerialName("payment_date")
    val paymentDate: Long,
    @SerialName("tenant_id")
    val tenantId: String,
    @SerialName("tenant_name")
    val tenantName: String,
    @SerialName("room_number")
    val roomNumber: Int,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("is_synced")
    val isSynced: Boolean,
    @SerialName("is_deleted")
    val isDeleted: Boolean,
    @SerialName("user_id")
    val userId: String
)
