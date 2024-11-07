package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview

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

@Preview(showBackground = true)
@Composable
private fun TypographyPreview() {
    TheColorTheme {
        Column {
            fun textForStyle(styleName: String) = "Preview of $styleName."
            val typography = MaterialTheme.typography
            Text(
                text = textForStyle("displayLarge"),
                style = typography.displayLarge,
            )
            Text(
                text = textForStyle("displayMedium"),
                style = typography.displayMedium,
            )
            Text(
                text = textForStyle("displaySmall"),
                style = typography.displaySmall,
            )
            Text(
                text = textForStyle("headlineLarge"),
                style = typography.headlineLarge,
            )
            Text(
                text = textForStyle("headlineMedium"),
                style = typography.headlineMedium,
            )
            Text(
                text = textForStyle("headlineSmall"),
                style = typography.headlineSmall,
            )
            Text(
                text = textForStyle("titleLarge"),
                style = typography.titleLarge,
            )
            Text(
                text = textForStyle("titleMedium"),
                style = typography.titleMedium,
            )
            Text(
                text = textForStyle("titleSmall"),
                style = typography.titleSmall,
            )
            Text(
                text = textForStyle("bodyLarge"),
                style = typography.bodyLarge,
            )
            Text(
                text = textForStyle("bodyMedium"),
                style = typography.bodyMedium,
            )
            Text(
                text = textForStyle("bodySmall"),
                style = typography.bodySmall,
            )
            Text(
                text = textForStyle("labelLarge"),
                style = typography.labelLarge,
            )
            Text(
                text = textForStyle("labelMedium"),
                style = typography.labelMedium,
            )
            Text(
                text = textForStyle("labelSmall"),
                style = typography.labelSmall,
            )
        }
    }
}