package com.engineerfred.easyrent.presentation.nav

sealed class AuthGraphDestinations(val dest: String) {

    data object SignUp: AuthGraphDestinations("sign_up")
    data object SignIn: AuthGraphDestinations("sign_in")
}