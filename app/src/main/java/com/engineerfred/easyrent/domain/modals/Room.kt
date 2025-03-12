package com.engineerfred.easyrent.domain.modals

import java.util.UUID

data class Room(
    val id: String = UUID.randomUUID().toString(),
    val roomType: String,
    val roomNumber: Int,
    val monthlyRent: Float,
    val isOccupied: Boolean = false,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val userId: String
)