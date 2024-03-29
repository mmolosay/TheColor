package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
internal fun ColorDetailsLoading(
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
}

@Composable
private fun Headline() {
    Box(
        modifier = Modifier
            .height(60.dp)
            .widthIn(max = 400.dp)
            .fillMaxWidth(fraction = 0.60f)
            .clipFullRounded()
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
                    .clipFullRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(82.dp)
                    .clipFullRounded()
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
                    .clipFullRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(96.dp)
                    .clipFullRounded()
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
                    .clipFullRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(94.dp)
                    .clipFullRounded()
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
                    .clipFullRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(96.dp)
                    .clipFullRounded()
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
                    .clipFullRounded()
                    .background(fill)
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(94.dp)
                    .clipFullRounded()
                    .background(fill)
            )
        }
    }
}

private val fill: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalColorsOnTintedSurface.current
        .accent
        .copy(alpha = 0.30f)

private fun Modifier.clipFullRounded() =
    clip(shape = RoundedCornerShape(percent = 100))

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