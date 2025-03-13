package com.engineerfred.easyrent.presentation.screens.add_tenant

import com.engineerfred.easyrent.domain.modals.Room


data class AddTenantUiState(
    val insertionErr: String? = null,
    val saving: Boolean = false,
    val name: String = "",
    val nameErr: String? = null,
    val balance: String = "",
    val contact: String = "",
    val contactErr: String? = null,
    val emergencyContact: String? = null,
    val notes: String? = null,
    val imageUrl: String? = null,
    val email: String = "",
    val emailErr: String? = null,
    val idDetails: String? = null,
    val saveSuccess: Boolean = false,
    val roomNumber: String? = null,
    val selectedRoomId: String? = null,
    val fetchingAvailableRooms: Boolean = false,
    val availableRooms: List<Room> = emptyList(),
)
