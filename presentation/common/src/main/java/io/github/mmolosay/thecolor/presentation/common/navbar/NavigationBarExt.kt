package io.github.mmolosay.thecolor.presentation.common.navbar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.WindowCompat

/*
 * Utils and extensions for navigation bar.
 */

// https://developer.android.com/about/versions/15/behavior-changes-15#window-insets
fun View.changeNavigationBar(
    @ColorInt color: Int?,
    useLightTintForControls: Boolean?,
) {
    if (this.isInEditMode) return
    val window = this.context.findActivityContext().window

    if (color != null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION") // doesn't recognize version check for some reason
            window.navigationBarColor = color
        }
    }
    if (useLightTintForControls != null) {
        WindowCompat.getInsetsController(window, this).run {
            /*
             * Won't take effect on APIs 35+.
             * The tint of controls depends on system Dark mode on/off and type of nav bar controls.
             */
            // for some reason, documentation of 'isAppearanceLightNavigationBars' doesn't match
            // it's behaviour, thus applying negation to the value
            this.isAppearanceLightNavigationBars = !useLightTintForControls
        }
    }
}

private fun Context.findActivityContext(): Activity {
    if (this is Activity) return this
    if (this is ContextWrapper) {
        val wrapped = this.baseContext
        return wrapped.findActivityContext()
    }
    error("This context doesn't belong to Activity")
}