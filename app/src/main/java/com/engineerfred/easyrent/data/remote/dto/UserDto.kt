package com.engineerfred.easyrent.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val email: String,
    @SerialName("tel_no")
    val telNo: String,
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @SerialName("hostel_name")
    val hostelName: String? = null
)
