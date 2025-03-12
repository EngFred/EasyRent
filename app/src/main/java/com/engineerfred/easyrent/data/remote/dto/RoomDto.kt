package com.engineerfred.easyrent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(
    @SerialName("id")
    val id: String,
    @SerialName("room_type")
    val roomType: String,
    @SerialName("room_number")
    val roomNumber: Int,
    @SerialName("monthly_rent")
    val monthlyRent: Float,
    @SerialName("is_occupied")
    val isOccupied: Boolean,
    @SerialName("is_deleted")
    val isDeleted: Boolean,
    @SerialName("is_synced")
    val isSynced: Boolean,
    @SerialName("user_id")
    val userId: String
)
