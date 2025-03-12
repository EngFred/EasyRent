package com.engineerfred.easyrent.presentation.screens.signup

data class SignUpState(
    val signingUp: Boolean = false,
    val signUpErr: String? = null,
    val email: String = "",
    val telNo: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val hostelName: String? = null,
    val imageUrl: String? = null,
    val firstNameErr: String? = null,
    val lastNameErr: String? = null,
    val emailErr: String? = null,
    val passwordErr: String? = null,
    val telNoErr: String? = null,
    val hostelNameErr: String? = null,
    val signUpSuccessful: Boolean = false
) {
    val ck = when {
        hostelName.isNullOrEmpty() -> true
        hostelName.isNotEmpty() && !hostelNameErr.isNullOrEmpty() -> false
        else -> true
    }
}
