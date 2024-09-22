package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TheColorTheme(
    theme: UiTheme = UiTheme.DayNight,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalIsDefaultNavigationBarLight provides theme.isDefaultNavigationBarLight(),
    ) {
        MaterialTheme(
            colorScheme = theme.colorScheme(),
            typography = typography(),
            content = content,
        )
    }
}