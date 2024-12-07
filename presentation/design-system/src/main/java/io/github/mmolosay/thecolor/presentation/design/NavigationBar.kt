package io.github.mmolosay.thecolor.presentation.design

import android.graphics.Color
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether the default appearance of navigation bar should have light tinted controls
 * to contrast against dark [LocalDefaultNavigationBarColor].
 */
val LocalDefaultShouldUseLightTintForNavBarControls =
    compositionLocalOf<Boolean> {
        error("CompositionLocal \"LocalDefaultShouldUseLightTintForNavBarControls\" doesn't have value by default.")
    }

/**
 * (Background) color of the default navigation bar's appearance.
 */
val LocalDefaultNavigationBarColor =
    staticCompositionLocalOf { Color.TRANSPARENT }

fun ColorScheme.shouldUseLightTintForNavBarControls(): Boolean {
    val colorSchemeBrightness = this.brightness()
    return when (colorSchemeBrightness) {
        Brightness.Light -> false
        Brightness.Dark -> true
    }
}