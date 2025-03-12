package com.engineerfred.easyrent.presentation.screens.add_room

sealed class AddRoomUiEvents {
    data class SelectedRoomType(val roomType: RoomType ) : AddRoomUiEvents()
    data class ChangedMonthlyPayment( val monthlyPayment: String ) :  AddRoomUiEvents()
    data class ChangedRoomNumber(val roomNumber: String ) : AddRoomUiEvents()
    data class CheckedOccupied(val isOccupied: Boolean) : AddRoomUiEvents()
    data object SaveClicked: AddRoomUiEvents()
}