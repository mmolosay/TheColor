package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.shimmer
import io.github.mmolosay.thecolor.presentation.design.LocalColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface

/**
 * Structurally repeats contents and arrangement of [ColorScheme].
 */
@Composable
internal fun ColorSchemeLoading(
    modifier: Modifier = Modifier,
) {
    val shimmerTheme = defaultShimmerTheme.copy(
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = LinearEasing,
                delayMillis = 200,
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )
    CompositionLocalProvider(
        LocalShimmerTheme provides shimmerTheme,
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .shimmer(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Swatches()

            Spacer(modifier = Modifier.height(20.dp))
            ModeSection()
        }
    }
}

@Composable
private fun Swatches() {
    Box(
        modifier = Modifier.graphicsLayer(alpha = fillAlpha),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy((-32).dp),
        ) {
            repeat(times = 5) {
                Swatch()
            }
        }
    }
}

@Composable
private fun Swatch() {
    val color = LocalColorsOnTintedSurface.current.accent
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun ModeSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .width(100.dp)
                .clipFullRounded()
                .background(fill)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Modes()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Modes() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ModeChip(
            modifier = Modifier.width(120.dp),
        )
        ModeChip(
            modifier = Modifier.width(160.dp),
        )
        ModeChip(
            modifier = Modifier.width(150.dp),
        )
        ModeChip(
            modifier = Modifier.width(180.dp),
        )
    }
}

@Composable
private fun ModeChip(modifier: Modifier) {
    Box(
        modifier = modifier
            .height(30.dp)
            .clipFullRounded()
            .background(fill)
    )
}

private val fill: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalColorsOnTintedSurface.current
        .accent
        .copy(alpha = fillAlpha)

private const val fillAlpha = 0.30f

private fun Modifier.clipFullRounded() =
    clip(shape = RoundedCornerShape(percent = 100))

@Preview
@Composable
private fun PreviewLight() =
    TheColorTheme {
        val colors = remember { colorsOnDarkSurface() }
        ProvideColorsOnTintedSurface(colors) {
            ColorSchemeLoading(
                modifier = Modifier.background(Color(0xFF_123456)),
            )
        }
    }

@Preview
@Composable
private fun PreviewDark() =
    TheColorTheme {
        val colors = remember { colorsOnLightSurface() }
        ProvideColorsOnTintedSurface(colors) {
            ColorSchemeLoading(
                modifier = Modifier.background(Color(0xFF_F0F8FF)),
            )
        }
    }