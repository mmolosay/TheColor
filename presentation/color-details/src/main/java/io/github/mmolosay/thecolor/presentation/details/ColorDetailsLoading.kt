package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.impl.compose.clipFullyRounded

/**
 * Structurally repeats contents and arrangement of [ColorDetails].
 */
@Composable
internal fun ColorDetailsLoading(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
            .fillMaxWidth()
            .shimmer(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Headline()

        Spacer(modifier = Modifier.height(28.dp))
        ColorTranslations()
    }
}

@Composable
private fun Headline() {
    Box(
        modifier = Modifier
            .height(60.dp)
            .widthIn(max = 400.dp)
            .fillMaxWidth(fraction = 0.60f)
            .clipFullyRounded()
            .background(fill)
    )
}

@Composable
private fun ColorTranslations() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // HEX
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(34.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(82.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
        }

        // RGB
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(38.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(96.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
        }

        // HSL
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(32.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(94.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
        }

        // HSV
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(34.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(96.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
        }

        // CMYK
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(42.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(94.dp)
                    .clipFullyRounded()
                    .background(fill)
            )
        }
    }
}

private val fill: Color
    @Composable
    @ReadOnlyComposable
    get() = colorsOnTintedSurface.accent.copy(alpha = 0.30f)

@Preview
@Composable
private fun PreviewLight() =
    TheColorTheme {
        val colors = remember { colorsOnDarkSurface() }
        ProvideColorsOnTintedSurface(colors) {
            ColorDetailsLoading(
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
            ColorDetailsLoading(
                modifier = Modifier.background(Color(0xFF_F0F8FF)),
            )
        }
    }