package com.engineerfred.easyrent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ExpenseDto(
    val id: String = UUID.randomUUID().toString(),
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val amount: Float,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    @SerialName("is_synced")
    val isSynced: Boolean,
    @SerialName("is_deleted")
    val isDeleted: Boolean
)
