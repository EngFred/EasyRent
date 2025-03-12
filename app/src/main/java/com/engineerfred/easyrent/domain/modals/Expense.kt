package com.engineerfred.easyrent.domain.modals

import java.util.UUID

data class Expense(
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
