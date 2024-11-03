package io.github.mmolosay.thecolor.presentation.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme as MaterialColorScheme

/**
 * A color scheme to be used in UI.
 * Entries of this enumeration are final, computed values that do not require any more data
 * (e.g. unlike 'DayNight' theme, see [DayNightColorSchemeResolver]).
 */
enum class ColorScheme {
    Light,
    Dark,
    ;
}

fun ColorScheme.toMaterialColorScheme(): MaterialColorScheme =
    when (this) {
        ColorScheme.Light -> lightColorScheme
        ColorScheme.Dark -> darkColorScheme
    }

private val lightColorScheme: MaterialColorScheme by lazy {
    lightColorScheme()
}

private val darkColorScheme: MaterialColorScheme by lazy {
    darkColorScheme()
}

@Composable
fun MaterialColorScheme.animateColors(): MaterialColorScheme {
    val animationSpec = tween<Color>(
        durationMillis = 400,
        easing = LinearOutSlowInEasing,
    )

    @Suppress("AnimateAsStateLabel")
    @Composable
    fun Color.animateAsState() =
        animateColorAsState(
            targetValue = this,
            animationSpec = animationSpec,
        )

    return this.copy(
        primary = this.primary.animateAsState().value,
        onPrimary = this.onPrimary.animateAsState().value,
        primaryContainer = this.primaryContainer.animateAsState().value,
        onPrimaryContainer = this.onPrimaryContainer.animateAsState().value,
        inversePrimary = this.inversePrimary.animateAsState().value,
        secondary = this.secondary.animateAsState().value,
        onSecondary = this.onSecondary.animateAsState().value,
        secondaryContainer = this.secondaryContainer.animateAsState().value,
        onSecondaryContainer = this.onSecondaryContainer.animateAsState().value,
        tertiary = this.tertiary.animateAsState().value,
        onTertiary = this.onTertiary.animateAsState().value,
        tertiaryContainer = this.tertiaryContainer.animateAsState().value,
        onTertiaryContainer = this.onTertiaryContainer.animateAsState().value,
        background = this.background.animateAsState().value,
        onBackground = this.onBackground.animateAsState().value,
        surface = this.surface.animateAsState().value,
        onSurface = this.onSurface.animateAsState().value,
        surfaceVariant = this.surfaceVariant.animateAsState().value,
        onSurfaceVariant = this.onSurfaceVariant.animateAsState().value,
        surfaceTint = this.surfaceTint.animateAsState().value,
        inverseSurface = this.inverseSurface.animateAsState().value,
        inverseOnSurface = this.inverseOnSurface.animateAsState().value,
        error = this.error.animateAsState().value,
        onError = this.onError.animateAsState().value,
        errorContainer = this.errorContainer.animateAsState().value,
        onErrorContainer = this.onErrorContainer.animateAsState().value,
        outline = this.outline.animateAsState().value,
        outlineVariant = this.outlineVariant.animateAsState().value,
        scrim = this.scrim.animateAsState().value,
        surfaceBright = this.surfaceBright.animateAsState().value,
        surfaceDim = this.surfaceDim.animateAsState().value,
        surfaceContainer = this.surfaceContainer.animateAsState().value,
        surfaceContainerHigh = this.surfaceContainerHigh.animateAsState().value,
        surfaceContainerHighest = this.surfaceContainerHighest.animateAsState().value,
        surfaceContainerLow = this.surfaceContainerLow.animateAsState().value,
        surfaceContainerLowest = this.surfaceContainerLowest.animateAsState().value,
    )
}