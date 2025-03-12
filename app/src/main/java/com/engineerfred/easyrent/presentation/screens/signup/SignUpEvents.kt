package com.engineerfred.easyrent.presentation.screens.signup

import android.content.ContentResolver

sealed class SignUpEvents {
    data class EmailChanged(val email: String) : SignUpEvents()
    data class TelNoChanged(val telNo: String) : SignUpEvents()
    data class PasswordChanged(val password: String) : SignUpEvents()
    data class FirstNameChanged(val firstName: String) : SignUpEvents()
    data class HostelNameChanged(val hostelName: String) : SignUpEvents()
    data class LastNameChanged(val lastName: String) : SignUpEvents()
    data class ImageUrlChangedChanged(val imageUrl: String) : SignUpEvents()
    data class SignUpButtonClicked(val contentResolver: ContentResolver) : SignUpEvents()
}
