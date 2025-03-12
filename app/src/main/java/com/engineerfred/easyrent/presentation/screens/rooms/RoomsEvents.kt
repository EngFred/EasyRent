package com.engineerfred.easyrent.presentation.screens.rooms

import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.modals.Tenant

sealed class RoomsEvents {
    data object ClickedRetryButton: RoomsEvents()
    data class RoomSelected(val roomId: String) : RoomsEvents()
    data class TenantDeleted(val tenant: Tenant, val roomId: String) : RoomsEvents()
    data class RoomDeleted(val room: Room) : RoomsEvents()
}