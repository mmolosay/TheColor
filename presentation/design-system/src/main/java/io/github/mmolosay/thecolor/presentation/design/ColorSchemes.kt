package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
internal fun UiTheme.colorScheme() =
    when (this) {
        UiTheme.Light -> lightColorScheme
        UiTheme.Dark -> darkColorScheme
    }

private val lightColorScheme by lazy {
    lightColorScheme()
}

private val darkColorScheme by lazy {
    darkColorScheme()
}