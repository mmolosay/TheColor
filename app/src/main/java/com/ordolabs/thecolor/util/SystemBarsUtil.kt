package com.ordolabs.thecolor.util

import android.app.Activity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat

fun AppCompatActivity.setTransparentSystemBars() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
}

fun Activity.setLightStatusBar(light: Boolean) {
    ViewCompat.getWindowInsetsController(window.decorView)?.run {
        isAppearanceLightStatusBars = light
    }
}

fun Activity.setLightNavigationBars(light: Boolean) {
    ViewCompat.getWindowInsetsController(window.decorView)?.run {
        isAppearanceLightNavigationBars = light
    }
}