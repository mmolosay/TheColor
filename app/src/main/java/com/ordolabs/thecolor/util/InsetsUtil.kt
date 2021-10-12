package com.ordolabs.thecolor.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowInsets
import android.view.WindowManager

object InsetsUtil {

    fun getNavigationBarHeight(context: Context?): Int? {
        context ?: return null
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= 30) {
            val insets = manager.currentWindowMetrics.windowInsets
            val type = WindowInsets.Type.navigationBars()
            return insets.getInsets(type).bottom
        } else {
            val display = manager.defaultDisplay
            val appSize = Point()
            val screenSize = Point()
            display?.apply {
                getSize(appSize)
                getRealSize(screenSize)
            }

            // navigation bar on the side
            if (appSize.x < screenSize.x) {
                return screenSize.x - appSize.x
            }
            // navigation bar at the bottom
            return if (appSize.y < screenSize.y) {
                screenSize.y - appSize.y
            } else 0
        }
    }
}