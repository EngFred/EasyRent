package com.engineerfred.easyrent.presentation.nav

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.engineerfred.easyrent.presentation.nav.Graphs.AUTH_GRAPH
import com.engineerfred.easyrent.presentation.nav.Graphs.MAIN_GRAPH

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootGraph(
    userId: String?,
    navController: NavHostController
) {

    LaunchedEffect(userId) {
        Log.i("MY_TAG", "RootGraph userId: $userId")
//        if( userId != null ) {
//            navController.navigate(MAIN_GRAPH) {
//                popUpTo(AUTH_GRAPH) {
//                    inclusive = true
//                }
//            }
//        }
    }

    NavHost(
        navController = navController,
        startDestination = if( userId == null ) AUTH_GRAPH else MAIN_GRAPH
    ) {
        authGraph(navController)
        mainGraph(navController)
    }

}