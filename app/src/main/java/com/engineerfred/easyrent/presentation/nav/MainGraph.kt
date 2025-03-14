package com.engineerfred.easyrent.presentation.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideInHorizontally
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.engineerfred.easyrent.presentation.screens.add_payment.AddPayment
import com.engineerfred.easyrent.presentation.screens.add_room.AddRoomScreen
import com.engineerfred.easyrent.presentation.screens.add_tenant.AddTenantScreen
import com.engineerfred.easyrent.presentation.screens.expenses.ExpensesScreen
import com.engineerfred.easyrent.presentation.screens.payments.Payments
import com.engineerfred.easyrent.presentation.screens.profile.Profile
import com.engineerfred.easyrent.presentation.screens.rooms.RoomsScreen
import com.engineerfred.easyrent.presentation.screens.tenants.Tenants


@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.mainGraph(
    navController: NavHostController
) {

    navigation(
        startDestination = MainScreens.RoomsList.dest,
        route = Graphs.MAIN_GRAPH
    ) {
        composable( route = MainScreens.RoomsList.dest ) {
            RoomsScreen(
                onAddRoom = {
                    navController.navigate(MainScreens.RoomDetails.createRoute(null)) {
                        launchSingleTop = true
                    }
                },
                onPaymentsClicked = {
                    navController.navigate(MainScreens.Payments.dest) {
                        launchSingleTop = true
                    }
                },
                onExpensesClicked = {
                    navController.navigate(MainScreens.Expenses.dest) {
                        launchSingleTop = true
                    }
                },
                onTenantsClicked = {
                    navController.navigate(MainScreens.Tenants.dest) {
                        launchSingleTop = true
                    }
                },
                onSettingsClicked = {
                    navController.navigate(MainScreens.Profile.dest) {
                        launchSingleTop = true
                    }
                },
                onAddTenant = { createdRoomId, monthlyRent, roomNumber ->
                    navController.navigate(MainScreens.TenantDetails.createRoute(tenantId = null, roomId = createdRoomId, monthlyRent = monthlyRent, roomNumber = roomNumber )) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = MainScreens.RoomDetails.dest,
            arguments = listOf(
                navArgument("roomId") {
                    type = NavType.StringType
                    nullable = true
                }
            ),
            enterTransition = {
                slideInHorizontally()
            }
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId")
            AddRoomScreen(
                roomId = roomId,
                onAddTenant = { createdRoomId, monthlyRent, roomNumber ->
                    navController.navigate(MainScreens.TenantDetails.createRoute(tenantId = null, roomId = createdRoomId, monthlyRent = monthlyRent, roomNumber = roomNumber ))
                },
                onCancel = {
                    navController.navigateUp()
                },
                onSaveCompleted = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            MainScreens.TenantDetails.dest,
            arguments = listOf(
                navArgument("tenantId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("roomId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("monthlyRent"){
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("roomNumber") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { navBackStackEntry ->
            val tenantId = navBackStackEntry.arguments?.getString("tenantId")
            val roomId = navBackStackEntry.arguments?.getString("roomId")
            val monthlyRent = navBackStackEntry.arguments?.getString("monthlyRent")
            val roomNumber = navBackStackEntry.arguments?.getString("roomNumber")

            AddTenantScreen(
                tenantId = tenantId,
                roomId = roomId,
                monthlyRent = monthlyRent,
                roomNumber = roomNumber,
                onSaveSuccessFromRoom = {
                    navController.navigate(MainScreens.RoomsList.dest){
                        popUpTo(MainScreens.RoomsList.dest){ inclusive = true }
                    }
                },
                onSaveSuccessFromTenants = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = MainScreens.Payments.dest
        ) {
            Payments(
                onAddPayment = {
                    navController.navigate(MainScreens.AddPayment.dest)
                },
                onBackClicked = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = MainScreens.Expenses.dest
        ) {
            ExpensesScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = MainScreens.Tenants.dest
        ) {
            Tenants(
                onAddTenant = {
                    navController.navigate(MainScreens.TenantDetails.createRoute(tenantId = null, roomId = null, monthlyRent = null, roomNumber = null ))
                },
                onBackClicked = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = MainScreens.Profile.dest
        ) {
            Profile(
                onSignOutSuccess = {
                    navController.navigate(Graphs.AUTH_GRAPH){
                        popUpTo(0) { inclusive = true } // Clears the entire back stack
                        launchSingleTop = true // Ensures a fresh start of AUTH_GRAPH
                    }
                },
                onBack = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = MainScreens.AddPayment.dest
        ) {
            AddPayment(
                onBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}