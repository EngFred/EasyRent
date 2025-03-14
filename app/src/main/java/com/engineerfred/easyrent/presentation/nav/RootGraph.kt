package com.engineerfred.easyrent.presentation.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.engineerfred.easyrent.presentation.nav.Graphs.AUTH_GRAPH
import com.engineerfred.easyrent.presentation.nav.Graphs.MAIN_GRAPH
import com.engineerfred.easyrent.presentation.theme.MySecondary
import com.engineerfred.easyrent.presentation.theme.MyTertiary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootGraph(
    userId: String?,
    navController: NavHostController
) {

    NavHost(
        modifier = Modifier.background(Brush.verticalGradient(listOf(MySecondary, MyTertiary))),
        navController = navController,
        startDestination = if( userId == null ) AUTH_GRAPH else MAIN_GRAPH
    ) {
        authGraph(navController)
        mainGraph(navController)
    }

}