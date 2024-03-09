package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
internal fun UiTheme.colorScheme() =
    when (this) {
        UiTheme.Light -> colorScheme(dark = false)
        UiTheme.Dark -> colorScheme(dark = true)
        UiTheme.DayNight -> colorScheme(dark = isSystemInDarkTheme())
    }

private fun colorScheme(dark: Boolean) =
    when (dark) {
        true -> darkColorScheme
        false -> lightColorScheme
    }

private val lightColorScheme by lazy {
    lightColorScheme()
}

private val darkColorScheme by lazy {
    darkColorScheme()
}