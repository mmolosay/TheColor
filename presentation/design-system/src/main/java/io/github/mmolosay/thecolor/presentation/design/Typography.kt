package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily

val LocalFontFamily = staticCompositionLocalOf { nunitoFamily }

val localFontFamily: FontFamily
    @Composable
    get() = LocalFontFamily.current

@Composable
internal fun typography() =
    MaterialTheme.typography.run {
        copy(
            displayLarge = displayLarge.copy(fontFamily = localFontFamily),
            displayMedium = displayMedium.copy(fontFamily = localFontFamily),
            displaySmall = displaySmall.copy(fontFamily = localFontFamily),
            headlineLarge = headlineLarge.copy(fontFamily = localFontFamily),
            headlineMedium = headlineMedium.copy(fontFamily = localFontFamily),
            headlineSmall = headlineSmall.copy(fontFamily = localFontFamily),
            titleLarge = titleLarge.copy(fontFamily = localFontFamily),
            titleMedium = titleMedium.copy(fontFamily = localFontFamily),
            titleSmall = titleSmall.copy(fontFamily = localFontFamily),
            bodyLarge = bodyLarge.copy(fontFamily = localFontFamily),
            bodyMedium = bodyMedium.copy(fontFamily = localFontFamily),
            bodySmall = bodySmall.copy(fontFamily = localFontFamily),
            labelLarge = labelLarge.copy(fontFamily = localFontFamily),
            labelMedium = labelMedium.copy(fontFamily = localFontFamily),
            labelSmall = labelSmall.copy(fontFamily = localFontFamily),
        )
    }