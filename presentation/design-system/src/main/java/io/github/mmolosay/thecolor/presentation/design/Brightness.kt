package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/** A binary enumeration of brightness values, light or dark. */
enum class Brightness {
    Light, Dark;
}

fun UiTheme.brightness() =
    when (this) {
        UiTheme.Light -> Brightness.Light
        UiTheme.Dark -> Brightness.Dark
    }

@Composable
fun systemBrightness() =
    when (isSystemInDarkTheme()) {
        true -> Brightness.Dark
        false -> Brightness.Light
    }