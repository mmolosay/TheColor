package io.github.mmolosay.thecolor.presentation.design

import android.graphics.Color
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalIsDefaultNavigationBarLight =
    compositionLocalOf<Boolean> {
        error("CompositionLocal \"LocalIsDefaultNavigationBarLight\" doesn't have value by default.")
    }

val LocalDefaultNavigationBarColor =
    staticCompositionLocalOf { Color.TRANSPARENT }

fun ColorScheme.isDefaultNavigationBarLight(): Boolean {
    val colorSchemeBrightness = this.brightness()
    return when (colorSchemeBrightness) {
        Brightness.Light -> true
        Brightness.Dark -> false
    }
}