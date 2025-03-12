package com.engineerfred.easyrent.presentation.screens.signin

data class SignInUIState(
    val signingIn: Boolean = false,
    val signInError: String? = null,
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val signInSuccessful: Boolean = false
)
