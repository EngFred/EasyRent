package com.engineerfred.easyrent.presentation.common

import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import android.view.Window
import com.engineerfred.easyrent.presentation.theme.MyPrimary
import com.engineerfred.easyrent.presentation.theme.MyTertiary

fun setStatusBarColors(
    window: Window,
    view: View,
    statusBarColor: Color = MyPrimary,
    navigationBarColor: Color = MyTertiary
) {
    window.statusBarColor = statusBarColor.toArgb()
    window.navigationBarColor = navigationBarColor.toArgb()
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
}