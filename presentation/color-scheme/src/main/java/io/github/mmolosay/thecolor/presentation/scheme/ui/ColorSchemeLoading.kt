package io.github.mmolosay.thecolor.presentation.scheme.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import io.github.mmolosay.thecolor.presentation.common.compose.clipFullyRounded
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface

/**
 * Structurally repeats contents and arrangement of [ColorScheme].
 */
@Composable
internal fun ColorSchemeLoading(
    modifier: Modifier = Modifier,
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

@Composable
private fun Swatches() {
    Box(
        modifier = Modifier.graphicsLayer(alpha = fillAlpha),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SwatchSpacing),
        ) {
            repeat(times = 7) {
                Swatch(
                    modifier = Modifier.scale(SwatchMinScale),
                )
            }
        }
    }
}

@Composable
private fun Swatch(
    modifier: Modifier = Modifier,
) =
    Box(
        modifier = modifier
            .size(SwatchSize)
            .clip(CircleShape)
            .background(colorsOnTintedSurface.accent),
    )

@Composable
private fun ModeSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .height(14.dp)
                .width(100.dp)
                .clipFullyRounded()
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
            .height(32.dp)
            .clipFullyRounded()
            .background(fill)
    )
}

private val fill: Color
    @Composable
    @ReadOnlyComposable
    get() = colorsOnTintedSurface.accent.copy(alpha = fillAlpha)

private const val fillAlpha = 0.30f

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