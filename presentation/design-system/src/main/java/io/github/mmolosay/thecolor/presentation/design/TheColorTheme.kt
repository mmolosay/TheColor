package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TheColorTheme(
    colorScheme: ColorScheme = DayNightColorSchemeResolver.resolve(systemBrightness()),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalIsDefaultNavigationBarLight provides colorScheme.isDefaultNavigationBarLight(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme.toMaterialColorScheme(),
            typography = typography(),
            content = content,
        )
    }
}