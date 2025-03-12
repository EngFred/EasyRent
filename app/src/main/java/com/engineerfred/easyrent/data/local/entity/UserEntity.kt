package com.engineerfred.easyrent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("userInfo")
data class UserInfoEntity(
    @PrimaryKey
    val id: String,
    val firstName: String,
    val lastName: String,
    val imageUrl: String? = null,
    val email: String,
    val telNo: String,
    val createdAt: Long = System.currentTimeMillis(),
    val hostelName: String? = null
)
