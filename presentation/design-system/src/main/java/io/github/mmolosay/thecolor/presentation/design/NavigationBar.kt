package io.github.mmolosay.thecolor.presentation.design

import android.graphics.Color
import androidx.compose.runtime.compositionLocalOf

val LocalIsDefaultNavigationBarLight =
    compositionLocalOf<Boolean> {
        error("CompositionLocal \"LocalIsDefaultNavigationBarLight\" doesn't have value by default.")
    }

// named as CompositionLocal for consistency
val LocalDefaultNavigationBarColor = Color.TRANSPARENT

fun UiTheme.isDefaultNavigationBarLight(): Boolean {
    val uiThemeTone = this.brightness()
    return when (uiThemeTone) {
        UiTheme.Brightness.Light -> true
        UiTheme.Brightness.Dark -> false
    }
}