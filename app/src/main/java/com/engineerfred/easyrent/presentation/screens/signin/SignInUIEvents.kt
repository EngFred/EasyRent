package com.engineerfred.easyrent.presentation.screens.signin

sealed class SignInUIEvents {
    data class EmailChanged(val email: String) : SignInUIEvents()
    data class PasswordChanged(val password: String) : SignInUIEvents()
    data object SignInButtonClicked : SignInUIEvents()
}