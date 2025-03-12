package com.engineerfred.easyrent.presentation.screens.profile

import com.engineerfred.easyrent.domain.modals.Payment
import com.engineerfred.easyrent.domain.modals.Room
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.domain.modals.UserInfoUpdateStatus

data class ProfileUIState(
    val isLoading: Boolean = true,
    val signingOut: Boolean = false,
    val signingOutSuccess: Boolean = false,
    val error: String? = null,
    val signOutErr: String? = null,
    val user: User? = null,
    val selectedImgUrl: String? = null,
    val newFirstName: String = "",
    val newLastName: String = "",
    val newHostelName: String = "",
    val newPhoneNumber: String = "",

    val firstNameErr: String? = null,
    val lastNameErr: String? = null,
    val hostelNameErr: String? = null,
    val phoneNameErr: String? = null,

    val makingChanges: Boolean = false,

    val updateState: UserInfoUpdateStatus? = null,
    val isUpdating: Boolean = false,
    val payments: List<Payment> = emptyList(),
    val rooms: List<Room> = emptyList(),
)
