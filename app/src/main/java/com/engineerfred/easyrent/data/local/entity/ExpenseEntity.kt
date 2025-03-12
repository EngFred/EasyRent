package com.engineerfred.easyrent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val amount: Float,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)
