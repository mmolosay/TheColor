package com.ordolabs.thecolor.util

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.use
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.google.android.material.color.MaterialColors
import com.ordolabs.thecolor.util.struct.Color
import com.ordolabs.thecolor.util.struct.isDark
import com.ordolabs.thecolor.util.struct.toColorInt
import android.graphics.Color as ColorAndroid

fun AppCompatActivity.setTransparentSystemBars() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
}

fun Activity.setLightStatusBar(light: Boolean) {
    ViewCompat.getWindowInsetsController(window.decorView)?.run {
        isAppearanceLightStatusBars = light
    }
}

fun Activity.setLightNavigationBar(light: Boolean) {
    ViewCompat.getWindowInsetsController(window.decorView)?.run {
        isAppearanceLightNavigationBars = light
    }
}

fun Activity.restoreNavigationBarColor() {
    val themeColor =
        MaterialColors.getColor(this, android.R.attr.navigationBarColor, ColorAndroid.WHITE)
    window.navigationBarColor = themeColor
    if (Build.VERSION.SDK_INT >= 27) {
        val attrs = intArrayOf(android.R.attr.windowLightNavigationBar)
        val isLightNavBar = this.theme.obtainStyledAttributes(attrs).use {
            it.getBoolean(0, /*defValue*/ true)
        }
        setLightNavigationBar(isLightNavBar)
    }
}

fun Activity.setNavigationBarColor(color: Color) {
    window.navigationBarColor = color.toColorInt()
    this.setLightNavigationBar(light = !color.isDark())
}