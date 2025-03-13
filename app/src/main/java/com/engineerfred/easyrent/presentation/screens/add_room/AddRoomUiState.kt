package com.engineerfred.easyrent.presentation.screens.add_room

data class AddRoomUiState(
    val selectedRoomType: RoomType = RoomType.Single,
    val monthlyPayment: String? = null,
    val monthlyPaymentErrMessage: String? = null,
    val isOccupied: Boolean = false,
    val roomNumber: String = "",
    val roomNumberErrMessage: String? = null,
    val insertionErr: String? = null,
    val createdRoomId: String? = null,
    val inserting: Boolean = false
)

enum class RoomType {
    Single,
    Double,
    Other
}
