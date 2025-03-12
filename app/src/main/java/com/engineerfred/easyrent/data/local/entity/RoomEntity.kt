package com.engineerfred.easyrent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
//    indices = [Index(value = ["roomNumber"], unique = true)]
)
data class RoomEntity(
    @PrimaryKey
    val id: String,
    val roomNumber: Int,
    val roomType: String,
    val monthlyRent: Float,
    val isOccupied: Boolean = false,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val userId: String
)
