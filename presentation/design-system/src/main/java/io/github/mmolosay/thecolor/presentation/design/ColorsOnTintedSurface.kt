package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// prefer using 'colorsOnTintedSurface' to get and 'ProvideColorsOnTintedSurface' to set
val LocalColorsOnTintedSurface = compositionLocalOf<ColorsOnTintedSurface> {
    error("CompositionLocal \"LocalColorsOnTintedSurface\" doesn't have value by default.")
}

val colorsOnTintedSurface: ColorsOnTintedSurface
    @Composable
    @ReadOnlyComposable
    inline get() = LocalColorsOnTintedSurface.current

@Composable
fun ProvideColorsOnTintedSurface(
    colors: ColorsOnTintedSurface,
    contentColor: Color = colors.accent,
    content: @Composable () -> Unit,
) =
    CompositionLocalProvider(
        LocalColorsOnTintedSurface provides colors,
        LocalContentColor provides contentColor, // for MaterialRippleTheme and ProminentRippleTheme
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

/*
 * Animating each color of MaterialColorScheme using 'animateAsState()' is expensive.
 * There's a room for improvement TODO: improve performance
 * Also see io.github.mmolosay.thecolor.presentation.design.ColorScheme -> MaterialColorScheme.animateColors()
 */
@Composable
fun ColorsOnTintedSurface.animate(
    animationSpec: AnimationSpec<Color> = spring(stiffness = Spring.StiffnessLow),
): ColorsOnTintedSurface {

    @Suppress("AnimateAsStateLabel")
    @Composable
    fun Color.animateAsState() =
        animateColorAsState(
            targetValue = this,
            animationSpec = animationSpec,
        )

    return ColorsOnTintedSurface(
        accent = this.accent.animateAsState().value,
        muted = this.muted.animateAsState().value,
    )
}