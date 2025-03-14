package com.engineerfred.easyrent.presentation.nav

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.engineerfred.easyrent.presentation.nav.Graphs.AUTH_GRAPH
import com.engineerfred.easyrent.presentation.nav.Graphs.MAIN_GRAPH
import com.engineerfred.easyrent.presentation.screens.signin.SignInScreen
import com.engineerfred.easyrent.presentation.screens.signup.SignUpScreen

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthScreens.SignIn.dest,
        route = AUTH_GRAPH
    ) {
        // Login Screen
        composable(
            AuthScreens.SignIn.dest,
        ) {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate(AuthScreens.SignUp.dest) { launchSingleTop = true } },
                onLoginSuccess = { navController.navigateToMainGraph() }
            )
        }

        // SignUp Screen
        composable(
            route = AuthScreens.SignUp.dest,
            enterTransition = {
                slideInHorizontally()
            },
            popExitTransition = {
                slideOutHorizontally()
            },
            exitTransition = {
                slideOutHorizontally()
            }
        ) {
            SignUpScreen(
                onNavigateToSignIn = { navController.navigate(AuthScreens.SignIn.dest) { launchSingleTop = true } },
                onSignUpSuccess = { navController.navigateToMainGraph() }
            )
        }
    }
}

fun NavHostController.navigateToMainGraph() {
    navigate(MAIN_GRAPH) {
        launchSingleTop = true
        popUpTo(AUTH_GRAPH) { inclusive = true }
    }
}