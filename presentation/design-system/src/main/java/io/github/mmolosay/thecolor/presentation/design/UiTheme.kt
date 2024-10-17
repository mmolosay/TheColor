package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * A UI color theme.
 * Entries of this enumeration are final, computed values that do not require any more data
 * (e.g. unlike 'DayNight' theme, see [DayNightUiThemeResolver]).
 */
enum class UiTheme {
    Light,
    Dark,
    ;

    enum class Brightness {
        Light, Dark;
    }
}

fun UiTheme.brightness() =
    when (this) {
        UiTheme.Light -> UiTheme.Brightness.Light
        UiTheme.Dark -> UiTheme.Brightness.Dark
    }

fun brightness(useDark: Boolean) =
    when (useDark) {
        true -> UiTheme.Brightness.Dark
        false -> UiTheme.Brightness.Light
    }

@Composable
fun systemBrightness() =
    brightness(useDark = isSystemInDarkTheme())