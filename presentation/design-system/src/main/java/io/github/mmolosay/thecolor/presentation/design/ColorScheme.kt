package io.github.mmolosay.thecolor.presentation.design

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import io.github.mmolosay.thecolor.presentation.design.Material3DynamicColorsAvailability.areDynamicColorsAvailable
import androidx.compose.material3.ColorScheme as MaterialColorScheme

/**
 * A color scheme to be used in UI.
 * Entries of this enumeration are final, computed values that do not require any more data
 * (e.g. unlike 'DayNight' theme, see [DayNightColorSchemeResolver]).
 */
enum class ColorScheme {
    Light,
    Dark,
    DynamicLight,
    DynamicDark,
    Jungle,
    Midnight,
    ;
}

fun ColorScheme.toMaterialColorScheme(context: Context): MaterialColorScheme {
    return when (this) {
        ColorScheme.Light -> lightColorScheme
        ColorScheme.Dark -> darkColorScheme
        ColorScheme.DynamicLight -> {
            if (!areDynamicColorsAvailable()) {
                error("$this cannot be used: Dynamic color schemes are available only on Android 12+")
            }
            dynamicLightColorScheme(context)
        }
        ColorScheme.DynamicDark -> {
            if (!areDynamicColorsAvailable()) {
                error("$this cannot be used: Dynamic color schemes are available only on Android 12+")
            }
            dynamicDarkColorScheme(context)
        }
        ColorScheme.Jungle -> jungleColorScheme
        ColorScheme.Midnight -> midnightColorScheme
    }
}

private val lightColorScheme: MaterialColorScheme by lazy {
    lightColorScheme()
}

private val darkColorScheme: MaterialColorScheme by lazy {
    darkColorScheme()
}

private val jungleColorScheme: MaterialColorScheme by lazy {
    MaterialColorScheme(
        primary = Color(0xFF3C6838),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFBDF0B3),
        onPrimaryContainer = Color(0xFF002203),
        inversePrimary = Color(0xFFA2D399),
        secondary = Color(0xFF53634E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD6E8CE),
        onSecondaryContainer = Color(0xFF111F0F),
        tertiary = Color(0xFF38656A),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBCEBF0),
        onTertiaryContainer = Color(0xFF002022),
        background = Color(0xFFF7FBF1),
        onBackground = Color(0xFF191D17),
        surface = Color(0xFFF7FBF1),
        onSurface = Color(0xFF191D17),
        surfaceVariant = Color(0xFFDEE5D8),
        onSurfaceVariant = Color(0xFF424940),
        surfaceTint = Color.Unspecified,
        inverseSurface = Color(0xFF2D322B),
        inverseOnSurface = Color(0xFFEFF2E9),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF73796F),
        outlineVariant = Color(0xFFC2C8BD),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFF7FBF1),
        surfaceDim = Color(0xFFD8DBD2),
        surfaceContainer = Color(0xFFECEFE6),
        surfaceContainerHigh = Color(0xFFE6E9E0),
        surfaceContainerHighest = Color(0xFFE0E4DA),
        surfaceContainerLow = Color(0xFFF2F5EB),
        surfaceContainerLowest = Color(0xFFFFFFFF),
    )
}

private val midnightColorScheme: MaterialColorScheme by lazy {
    MaterialColorScheme(
        primary = Color(0xFF9CCBFB),
        onPrimary = Color(0xFF003354),
        primaryContainer = Color(0xFF114A73),
        onPrimaryContainer = Color(0xFFCFE5FF),
        inversePrimary = Color(0xFF31628D),
        secondary = Color(0xFFB9C8DA),
        onSecondary = Color(0xFF243240),
        secondaryContainer = Color(0xFF3A4857),
        onSecondaryContainer = Color(0xFFD5E4F7),
        tertiary = Color(0xFFD4BEE6),
        onTertiary = Color(0xFF392A49),
        tertiaryContainer = Color(0xFF504061),
        onTertiaryContainer = Color(0xFFEFDBFF),
        background = Color(0xFF101418),
        onBackground = Color(0xFFE0E2E8),
        surface = Color(0xFF101418),
        onSurface = Color(0xFFE0E2E8),
        surfaceVariant = Color(0xFF42474E),
        onSurfaceVariant = Color(0xFFC2C7CF),
        surfaceTint = Color.Unspecified,
        inverseSurface = Color(0xFFE0E2E8),
        inverseOnSurface = Color(0xFF2D3135),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF8C9199),
        outlineVariant = Color(0xFF42474E),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF36393E),
        surfaceDim = Color(0xFF101418),
        surfaceContainer = Color(0xFF1C2024),
        surfaceContainerHigh = Color(0xFF272A2F),
        surfaceContainerHighest = Color(0xFF32353A),
        surfaceContainerLow = Color(0xFF181C20),
        surfaceContainerLowest = Color(0xFF0B0E12),
    )
}

@Composable
fun MaterialColorScheme.animateColors(): MaterialColorScheme {
    val target = this
    var animated by remember { mutableStateOf(target) }
    LaunchedEffect(target) {
        val source = animated
        if (source == target) return@LaunchedEffect
        val animatable = Animatable(initialValue = 0f)
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) {
            animated = lerp(source, target, value)
        }
    }
    return animated
}

private fun lerp(
    start: MaterialColorScheme,
    stop: MaterialColorScheme,
    fraction: Float,
): MaterialColorScheme {
    fun lerp(color: MaterialColorScheme.() -> Color): Color =
        lerp(
            start = start.color(),
            stop = stop.color(),
            fraction = fraction,
        )
    return MaterialColorScheme(
        primary = lerp { primary },
        onPrimary = lerp { onPrimary },
        primaryContainer = lerp { primaryContainer },
        onPrimaryContainer = lerp { onPrimaryContainer },
        inversePrimary = lerp { inversePrimary },
        secondary = lerp { secondary },
        onSecondary = lerp { onSecondary },
        secondaryContainer = lerp { secondaryContainer },
        onSecondaryContainer = lerp { onSecondaryContainer },
        tertiary = lerp { tertiary },
        onTertiary = lerp { onTertiary },
        tertiaryContainer = lerp { tertiaryContainer },
        onTertiaryContainer = lerp { onTertiaryContainer },
        background = lerp { background },
        onBackground = lerp { onBackground },
        surface = lerp { surface },
        onSurface = lerp { onSurface },
        surfaceVariant = lerp { surfaceVariant },
        onSurfaceVariant = lerp { onSurfaceVariant },
        surfaceTint = lerp { surfaceTint },
        inverseSurface = lerp { inverseSurface },
        inverseOnSurface = lerp { inverseOnSurface },
        error = lerp { error },
        onError = lerp { onError },
        errorContainer = lerp { errorContainer },
        onErrorContainer = lerp { onErrorContainer },
        outline = lerp { outline },
        outlineVariant = lerp { outlineVariant },
        scrim = lerp { scrim },
        surfaceBright = lerp { surfaceBright },
        surfaceDim = lerp { surfaceDim },
        surfaceContainer = lerp { surfaceContainer },
        surfaceContainerHigh = lerp { surfaceContainerHigh },
        surfaceContainerHighest = lerp { surfaceContainerHighest },
        surfaceContainerLow = lerp { surfaceContainerLow },
        surfaceContainerLowest = lerp { surfaceContainerLowest },
    )
}