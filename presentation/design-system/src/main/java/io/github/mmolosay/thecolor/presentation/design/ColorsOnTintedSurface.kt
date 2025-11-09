package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// defined as 'private' to enforce usage of higher-level APIs, like 'colorsOnTintedSurface' and 'ProvideColorsOnTintedSurface'
private val LocalColorsOnTintedSurface = compositionLocalOf<ColorsOnTintedSurface> {
    error("CompositionLocal \"LocalColorsOnTintedSurface\" doesn't have value by default.")
}

val colorsOnTintedSurface: ColorsOnTintedSurface
    @Composable
    @ReadOnlyComposable
    get() = LocalColorsOnTintedSurface.current

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
 * Doesn't depend on current [MaterialTheme.colorScheme].
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

@Composable
fun ColorsOnTintedSurface.animate(
    animationSpec: AnimationSpec<Float> = spring(),
): ColorsOnTintedSurface {
    val target = this
    var animated by remember { mutableStateOf(target) }
    LaunchedEffect(target) {
        val source = animated
        if (source == target) return@LaunchedEffect
        val animatable = Animatable(initialValue = 0f)
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = animationSpec,
        ) {
            animated = lerp(source, target, value)
        }
    }
    return animated
}

private fun lerp(
    start: ColorsOnTintedSurface,
    stop: ColorsOnTintedSurface,
    fraction: Float,
): ColorsOnTintedSurface {
    fun lerp(color: ColorsOnTintedSurface.() -> Color): Color =
        androidx.compose.ui.graphics.lerp(
            start = start.color(),
            stop = stop.color(),
            fraction = fraction,
        )
    return ColorsOnTintedSurface(
        accent = lerp { accent },
        muted = lerp { muted },
    )
}