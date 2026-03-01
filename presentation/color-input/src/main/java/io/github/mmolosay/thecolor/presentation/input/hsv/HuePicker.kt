package io.github.mmolosay.thecolor.presentation.input.hsv

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalSlider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorUtils.HsvColor
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorUtils.HsvHueRange
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorUtils.HsvSaturationRange
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorUtils.HsvValueRange
import kotlin.math.nextDown

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HuePicker(
    modifier: Modifier = Modifier,
    hue: HueValue,
    onChange: (HueValue) -> Unit,
    trackShape: Shape = RoundedCornerShape(size = 4.dp),
) {
    @Suppress("NAME_SHADOWING") // intentional name shadowing to enforce using this wrapped lambda instead of the one passed in arguments
    val onChange by rememberUpdatedState(onChange) // preventive measure to avoid possible redraws due to new lambda
    val sliderState = rememberSliderState(
        value = hue.value,
        valueRange = with(HsvHueRange) { start..endExclusive.nextDown() },
    )
    val sliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.onBackground,
    )
    val interactionSource = remember { MutableInteractionSource() }
    VerticalSlider(
        modifier = modifier,
        state = sliderState,
        colors = sliderColors,
        interactionSource = interactionSource,
        thumb = { sliderState ->
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                sliderState = sliderState,
                colors = sliderColors,
                thumbSize = DpSize(width = 24.dp, height = 4.dp),
            )
        },
        track = { _ ->
            HueBar(
                modifier = Modifier
                    .width(16.dp) // same as 'internal val TrackHeight' in 'androidx.compose.material3.Slider.kt'
                    .clip(trackShape),
            )
        },
    )
    LaunchedEffect(hue) {
        sliderState.value = hue.value
    }
    LaunchedEffect(sliderState) {
        snapshotFlow { sliderState.value }.collect { newHue ->
            onChange(HueValue(newHue))
        }
    }
}

@Composable
private fun HueBar(
    modifier: Modifier = Modifier,
) {
    val hues = remember { makeListOfPureHues() }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        val gradient = Brush.verticalGradient(hues)
        drawRect(gradient)
    }
}

private fun makeListOfPureHues(): List<Color> {
    val totalHues = 360
    val maxHueValue = HsvHueRange.endExclusive
    val hueStep = (maxHueValue / totalHues)
    // reuse the same array to reduce allocations
    val hsvComponents = floatArrayOf(
        /*h*/ Float.NEGATIVE_INFINITY,
        /*s*/ HsvSaturationRange.endInclusive,
        /*v*/ HsvValueRange.endInclusive,
    )
    return List(totalHues) { index ->
        val hue = index * hueStep
        hsvComponents[0] = hue
        HsvColor(components = hsvComponents)
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides 0.dp, // 'VerticalSlider' uses 'Modifier.minimumInteractiveComponentSize()', and the default value is 48dp
            ) {
                HuePicker(
                    modifier = Modifier.height(192.dp),
                    hue = HueValue(117f),
                    onChange = {},
                )
            }
        }
    }
}