package com.engineerfred.easyrent.presentation.nav

sealed class AuthScreens(val dest: String) {

    data object SignUp: AuthScreens("sign_up")
    data object SignIn: AuthScreens("sign_in")
}