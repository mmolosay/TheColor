package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun TheColorTheme(
    theme: UiTheme = UiTheme.DayNight,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = theme.colorScheme(),
        content = content,
    )
}

enum class UiTheme {
    Light,
    Dark,
    DayNight,
}