package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalColorsOnTintedSurface = compositionLocalOf { colorsOnLightSurface() }

val colorsOnTintedSurface: ColorsOnTintedSurface
    @Composable
    get() = LocalColorsOnTintedSurface.current

/**
 * Collection of colors to be used on surface of no particular color.
 * They are guaranteed to be contrast against the color of background.
 *
 * Doesn't depend on current [UiTheme].
 */
data class ColorsOnTintedSurface(
    val accent: Color,
    val muted: Color,
)

fun colorsOnLightSurface(
    accent: Color = Color(0xDE_000000),
    muted: Color = Color(0x99_000000),
) =
    ColorsOnTintedSurface(
        accent = accent,
        muted = muted,
    )

fun colorsOnDarkSurface(
    accent: Color = Color(0xFF_FFFFFF),
    muted: Color = Color(0x99_FFFFFF),
) =
    ColorsOnTintedSurface(
        accent = accent,
        muted = muted,
    )