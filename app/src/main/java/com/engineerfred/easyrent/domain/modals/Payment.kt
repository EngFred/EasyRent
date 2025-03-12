package com.engineerfred.easyrent.domain.modals

import java.util.UUID

data class Payment(
    val id: String = UUID.randomUUID().toString(),
    val amount: Float,
    val newBalance: Float,
    val paymentDate: Long = System.currentTimeMillis(),
    val tenantId: String,
    val tenantName: String,
    val roomNumber: Int,
    val paymentMethod: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val userId: String
)

enum class PaymentMethod( val displayName: String ) {
    MobileMoney("Mobile Money"),
    CreditCard("Credit Card"),
    Cash("Cash")
}

