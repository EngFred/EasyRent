package com.engineerfred.easyrent.presentation.screens.rooms

import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.modals.Tenant

data class RoomsState(
    val loading: Boolean = true,
    val rooms: List<Room> = emptyList(),
    val unSyncedRooms: List<Room> = emptyList(),
    val error: String? = null,
    val roomId: String? = null,
    val tenant: Tenant? = null,
    val isDeletingTenant: Boolean = false,
    val deletingTenantErr: String? = null,
    val deleteTenantSuccessful: Boolean = false,
    val fetchingRoomTenant: Boolean = false,

    val deletingRoom: Boolean = false,
    val deletedRoomId: String? = null,
    val deletingRoomErr: String? = null,

    val showSyncButton: Boolean = false,
    val showSyncRequired: Boolean = false
)
