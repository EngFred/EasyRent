package com.engineerfred.easyrent.domain.modals

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val imageUrl: String? = null,
    val email: String,
    val telNo: String,
    val createdAt: Long = System.currentTimeMillis(),
    val hostelName: String? = null
)
