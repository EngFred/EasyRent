package com.engineerfred.easyrent.presentation.screens.add_tenant

import android.content.ContentResolver
import com.engineerfred.easyrent.domain.modals.Room

sealed class AddTenantUiEvents {
    data class NameChanged(val name: String ) : AddTenantUiEvents()
    data class EmailChanged(val email: String ) : AddTenantUiEvents()
    data class BalanceChanged(val balance: String ) : AddTenantUiEvents()
    data class SelectedRoomNumber(val roomNumber: String ) : AddTenantUiEvents()
    data class SelectedRoomId(val roomId: String ) : AddTenantUiEvents()
    data class ContactChanged(val contact: String ) :  AddTenantUiEvents()
    data class EmergencyContactChanged(val emergencyContact: String ) : AddTenantUiEvents()
    data class ChangedIdDetails(val idDetails: String) : AddTenantUiEvents()
    data class NotesChanged(val notes: String) : AddTenantUiEvents()
    data class ImageUrlChanged(val imageUrl: String?) : AddTenantUiEvents()
    data object FetchedAvailableRooms : AddTenantUiEvents()
    data class RoomSelected(val room: Room) : AddTenantUiEvents()
    data class SaveClicked(val contentResolver: ContentResolver): AddTenantUiEvents()
}