package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/** A binary enumeration of brightness values, light or dark. */
enum class Brightness {
    Light, Dark;
}

fun ColorScheme.brightness() =
    when (this) {
        ColorScheme.Light -> Brightness.Light
        ColorScheme.Dark -> Brightness.Dark
    }

@Composable
fun systemBrightness() =
    when (isSystemInDarkTheme()) {
        true -> Brightness.Dark
        false -> Brightness.Light
    }