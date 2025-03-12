package com.engineerfred.easyrent

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.engineerfred.easyrent.presentation.theme.EasyRentTheme
import com.engineerfred.easyrent.domain.repository.PreferencesRepository
import com.engineerfred.easyrent.presentation.nav.RootGraph
import com.engineerfred.easyrent.util.WorkerUtils
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: Auth

    @Inject
    lateinit var prefs: PreferencesRepository

    @Inject
    lateinit var workManager: WorkManager

    private var userId: String? = ""

    override fun onResume() {
        super.onResume()
        requestPermission()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                userId != ""
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WorkerUtils.scheduleSyncWorkers(workManager)
        lifecycleScope.launch {
            repeatOnLifecycle( Lifecycle.State.STARTED ) {
                userId = prefs.getUserId().firstOrNull()
            }
        }

        setContent {
            val navController = rememberNavController()
            EasyRentTheme {
                Scaffold { p ->
                    Log.i("TAG", "$p")
                    RootGraph(auth = auth, navController = navController)
                }
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}


