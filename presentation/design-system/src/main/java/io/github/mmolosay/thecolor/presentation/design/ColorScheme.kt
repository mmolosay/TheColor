package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/**
 * A color scheme to be used in UI.
 * Entries of this enumeration are final, computed values that do not require any more data
 * (e.g. unlike 'DayNight' theme, see [DayNightColorSchemeResolver]).
 */
enum class ColorScheme {
    Light,
    Dark,
    ;
}

internal fun ColorScheme.toMaterialColorScheme() =
    when (this) {
        ColorScheme.Light -> lightColorScheme
        ColorScheme.Dark -> darkColorScheme
    }

private val lightColorScheme by lazy {
    lightColorScheme()
}

private val darkColorScheme by lazy {
    darkColorScheme()
}