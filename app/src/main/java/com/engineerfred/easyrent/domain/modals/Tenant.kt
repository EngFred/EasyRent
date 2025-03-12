package com.engineerfred.easyrent.domain.modals

import java.util.UUID

data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val contact: String,
    val email: String? = null,
    val balance: Float,
    val moveInDate: Long = System.currentTimeMillis(),
    val roomId: String,
    val roomNumber: Int,
    val emergencyContact: String? = null,
    val idDetails: String? = null,
    val notes: String? = null,
    val profilePic: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val userId: String
)