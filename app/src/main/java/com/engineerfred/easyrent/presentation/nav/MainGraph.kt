package com.engineerfred.easyrent.presentation.nav

import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.work.WorkManager
import com.engineerfred.easyrent.domain.modals.User
import com.engineerfred.easyrent.presentation.screens.add_payment.AddPayment
import com.engineerfred.easyrent.presentation.screens.add_room.AddRoomScreen
import com.engineerfred.easyrent.presentation.screens.add_tenant.AddTenantScreen
import com.engineerfred.easyrent.presentation.screens.expenses.ExpensesScreen
import com.engineerfred.easyrent.presentation.screens.image_view.ImageViewScreen
import com.engineerfred.easyrent.presentation.screens.payments.Payments
import com.engineerfred.easyrent.presentation.screens.profile.Profile
import com.engineerfred.easyrent.presentation.screens.rooms.RoomsScreen
import com.engineerfred.easyrent.presentation.screens.tenants.Tenants

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    workManager: WorkManager,
    user: User?
) {

    navigation(
        startDestination = MainGraphDestinations.RoomsList.dest,
        route = Graphs.MAIN_GRAPH
    ) {

        //rooms
        composable(
            route = MainGraphDestinations.RoomsList.dest,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 500)
                )
            },
        ) {
            RoomsScreen(
                onAddRoom = {
                    navController.navigate(MainGraphDestinations.RoomDetails.createRoute(null)) {
                        launchSingleTop = true
                    }
                },
                onPaymentsClicked = {
                    navController.navigate(MainGraphDestinations.Payments.dest) {
                        launchSingleTop = true
                    }
                },
                onExpensesClicked = {
                    navController.navigate(MainGraphDestinations.Expenses.dest) {
                        launchSingleTop = true
                    }
                },
                onTenantsClicked = {
                    navController.navigate(MainGraphDestinations.Tenants.dest) {
                        launchSingleTop = true
                    }
                },
                onSettingsClicked = {
                    navController.navigate(MainGraphDestinations.Profile.dest) {
                        launchSingleTop = true
                    }
                },
                onAddTenant = { createdRoomId, monthlyRent, roomNumber ->
                    navController.navigate(MainGraphDestinations.TenantDetails.createRoute(tenantId = null, roomId = createdRoomId, monthlyRent = monthlyRent, roomNumber = roomNumber )) {
                        launchSingleTop = true
                    }
                },
                workManager = workManager
            )
        }

        //room upsert
        composable(
            route = MainGraphDestinations.RoomDetails.dest,
            arguments = listOf(
                navArgument("roomId") {
                    type = NavType.StringType
                    nullable = true
                }
            ),
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId")
            AddRoomScreen(
                roomId = roomId,
                onAddTenant = { createdRoomId, monthlyRent, roomNumber ->
                    navController.navigate(MainGraphDestinations.TenantDetails.createRoute(tenantId = null, roomId = createdRoomId, monthlyRent = monthlyRent, roomNumber = roomNumber ))
                },
                onCancel = {
                    navController.navigateUp()
                },
                onSaveCompleted = {
                    navController.navigateUp()
                }
            )
        }

        //tenant upsert
        composable(
            route = MainGraphDestinations.TenantDetails.dest,
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
            ),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 500)
                )
            },
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
                    navController.navigate(MainGraphDestinations.RoomsList.dest){
                        popUpTo(MainGraphDestinations.RoomsList.dest){ inclusive = true }
                    }
                },
                onSaveSuccessFromTenants = {
                    navController.navigateUp()
                }
            )
        }

        //payments
        composable(
            route = MainGraphDestinations.Payments.dest,
        ) {
            Payments(
                onAddPayment = {
                    navController.navigate(MainGraphDestinations.AddPayment.dest)
                },
                onBackClicked = {
                    navController.navigateUp()
                },
                workManager = workManager
            )
        }

        //expenses
        composable(
            route = MainGraphDestinations.Expenses.dest
        ) {
            ExpensesScreen(
                onBack = { navController.navigateUp() },
                workManager = workManager
            )
        }

        //tenants
        composable(
            route = MainGraphDestinations.Tenants.dest
        ) {
            Tenants(
                onAddTenant = {
                    navController.navigate(MainGraphDestinations.TenantDetails.createRoute(tenantId = null, roomId = null, monthlyRent = null, roomNumber = null ))
                },
                onBackClicked = {
                    navController.navigateUp()
                },
                workManager  = workManager,
                onImageClicked = {
                    val imageUrl = Uri.encode(it)
                    navController.navigate(MainGraphDestinations.ImageView.createRoute(imageUrl)) {
                        launchSingleTop = true
                    }
                },
                user = user
            )
        }

        //profile
        composable(
            route = MainGraphDestinations.Profile.dest
        ) {
            Profile(
                onSignOutSuccess = {
                    navController.navigate(Graphs.AUTH_GRAPH){
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = {
                    navController.navigateUp()
                },
                onImageClicked = {
                    val imageUrl = Uri.encode(it)
                    navController.navigate(MainGraphDestinations.ImageView.createRoute(imageUrl))
                }
            )
        }

        composable(
            route = MainGraphDestinations.AddPayment.dest,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 500)
                )
            },
        ) {
            AddPayment(
                onBack = {
                    navController.navigateUp()
                }
            )
        }

        //image view screen
        composable(
            route = MainGraphDestinations.ImageView.dest,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 600)
                )
            },
            arguments = listOf(
                navArgument("imageUrl") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedImageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            val decodedImageUrl = Uri.decode(encodedImageUrl)
            ImageViewScreen(imageUrl = decodedImageUrl, onBack = {
                navController.navigateUp()
            })
        }
    }
}