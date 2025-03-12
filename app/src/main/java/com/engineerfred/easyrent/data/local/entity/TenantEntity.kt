package com.engineerfred.easyrent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "tenants"
    //indices = [Index(value = ["roomId"], unique = true)]
)
data class TenantEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val contact: String,
    val email: String? = null,
    val balance: Float,
    val moveInDate: Long = System.currentTimeMillis(),
    val roomId: String,
    val roomNumber: Int, //for display on tenants list
    val emergencyContact: String? = null,
    val idDetails: String? = null,
    val notes: String? = null,
    val profilePic: String? = null,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
    val userId: String
)