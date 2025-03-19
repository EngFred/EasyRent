package com.engineerfred.easyrent.presentation.nav

import androidx.compose.animation.core.tween
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
        startDestination = AuthGraphDestinations.SignIn.dest,
        route = AUTH_GRAPH
    ) {
        // Login Screen
        composable(
            AuthGraphDestinations.SignIn.dest,
        ) {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate(AuthGraphDestinations.SignUp.dest) { launchSingleTop = true } },
                onLoginSuccess = { navController.navigateToMainGraph() }
            )
        }

        // SignUp Screen
        composable(
            route = AuthGraphDestinations.SignUp.dest,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 500)
                )
            },
        ) {
            SignUpScreen(
                onNavigateToSignIn = { navController.navigate(AuthGraphDestinations.SignIn.dest) { launchSingleTop = true } },
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