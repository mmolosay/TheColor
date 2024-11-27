package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.ColorScheme as MaterialColorScheme

@Composable
fun TheColorTheme(
    colorScheme: ColorScheme = DayNightColorSchemeResolver.resolve(systemBrightness()),
    content: @Composable () -> Unit,
) {
    TheColorTheme(
        materialColorScheme = colorScheme.toMaterialColorScheme(),
        useLightTintForNavBarControls = colorScheme.shouldUseLightTintForNavBarControls(),
        content = content,
    )
}

@Composable
fun TheColorTheme(
    materialColorScheme: MaterialColorScheme,
    useLightTintForNavBarControls: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDefaultShouldUseLightTintForNavBarControls provides useLightTintForNavBarControls,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = typography(),
            content = content,
        )
    }
}