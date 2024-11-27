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

fun View.changeNavigationBar(
    @ColorInt color: Int,
    isLight: Boolean,
) {
    if (this.isInEditMode) return
    val window = this.context.findActivityContext().window
    window.navigationBarColor = color
    WindowCompat.getInsetsController(window, this).run {
        this.isAppearanceLightNavigationBars = isLight
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