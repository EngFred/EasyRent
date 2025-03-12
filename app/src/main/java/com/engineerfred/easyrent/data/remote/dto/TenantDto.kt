package com.engineerfred.easyrent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TenantDto(
    val id: String,
    val name: String,
    val contact: String,
    val email: String? = null,
    val balance: Float,
    @SerialName("move_in_date")
    val moveInDate: Long = System.currentTimeMillis(),
    @SerialName("room_id")
    val roomId: String,
    @SerialName("room_number")
    val roomNumber: Int,
    @SerialName("emergency_contact")
    val emergencyContact: String? = null,
    @SerialName("id_details")
    val idDetails: String? = null,
    val notes: String? = null,
    @SerialName("profile_pic")
    val profilePic: String? = null,
    @SerialName("is_synced")
    val isSynced: Boolean,
    @SerialName("is_deleted")
    val isDeleted: Boolean,
    @SerialName("user_id")
    val userId: String
)