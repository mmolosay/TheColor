package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalColorsOnTintedSurface = compositionLocalOf<ColorsOnTintedSurface> {
    error("CompositionLocal \"LocalColorsOnTintedSurface\" doesn't have value by default.")
}

val colorsOnTintedSurface: ColorsOnTintedSurface
    @Composable
    get() = LocalColorsOnTintedSurface.current

@Composable
fun ProvideColorsOnTintedSurface(
    colors: ColorsOnTintedSurface,
    contentColor: Color = colors.accent,
    content: @Composable () -> Unit,
) =
    CompositionLocalProvider(
        LocalColorsOnTintedSurface provides colors,
        LocalContentColor provides contentColor, // for MaterialRippleTheme
        content = content,
    )

/**
 * Collection of colors to be used on surface of no particular (known beforehand) color.
 * They are guaranteed to be contrast against the color of background (surface).
 *
 * Doesn't depend on current [ColorScheme].
 *
 * @see colorsOnLightSurface
 * @see colorsOnDarkSurface
 */
data class ColorsOnTintedSurface(
    val accent: Color,
    val muted: Color,
)

/**
 * Dark colors to contrast against light background.
 */
fun colorsOnLightSurface(
    accent: Color = Color(0xDE_000000),
    muted: Color = Color(0x99_000000),
) =
    ColorsOnTintedSurface(
        accent = accent,
        muted = muted,
    )

/**
 * Light colors to contrast against dark background.
 */
fun colorsOnDarkSurface(
    accent: Color = Color(0xFF_FFFFFF),
    muted: Color = Color(0x99_FFFFFF),
) =
    ColorsOnTintedSurface(
        accent = accent,
        muted = muted,
    )