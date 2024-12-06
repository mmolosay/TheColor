package io.github.mmolosay.thecolor.presentation.impl

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
        window.navigationBarColor = color
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