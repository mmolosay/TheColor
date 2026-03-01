package io.github.mmolosay.thecolor.presentation.input.hsv

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastFirst
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.hsv.HsvColorUtils.HsvColor
import io.github.mmolosay.thecolor.presentation.input.hsv.HsvColorUtils.HsvHueRange
import io.github.mmolosay.thecolor.presentation.input.hsv.HsvColorUtils.HsvSaturationRange
import io.github.mmolosay.thecolor.presentation.input.hsv.HsvColorUtils.HsvValueRange
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Color as ComposeColor
import io.github.mmolosay.thecolor.domain.model.Color as DomainColor

@Composable
internal fun SaturationAndValuePicker(
    hue: HueValue,
    sv: SaturationAndValue,
    onChange: (SaturationAndValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("NAME_SHADOWING") // intentional name shadowing to enforce using this wrapped lambda instead of the one passed in arguments
    val onChange by rememberUpdatedState(onChange) // preventive measure to avoid possible redraws due to new lambda
    var mapSize: Size? by remember { mutableStateOf(null) }
    fun rawPointerPositionToSv(rawPos: Offset): SaturationAndValue? {
        val mapSize = mapSize ?: return null // hasn't been measured yet, so can't process the position
        val coercedPosition = Offset(
            x = rawPos.x.coerceIn(0f, mapSize.width),
            y = rawPos.y.coerceIn(0f, mapSize.height),
        )
        return SaturationAndValue(
            saturation = coercedPosition.x / mapSize.width,
            value = (mapSize.height - coercedPosition.y) / mapSize.height,
        )
    }
    fun onPointerPositionChange(rawPos: Offset) {
        val pointerSv = rawPointerPositionToSv(rawPos)
        if (pointerSv != null) {
            onChange(pointerSv)
        }
    }
    Layout(
        modifier = modifier,
        content = {
            SaturationAndValueMap(
                modifier = Modifier
                    .layoutId(Components.Map)
                    .onSizeChanged {
                        mapSize = it.toSize()
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { position ->
                                onPointerPositionChange(position)
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                onPointerPositionChange(change.position)
                            },
                        )
                    },
                hue = hue,
            )
            Pointer(
                modifier = Modifier
                    .layoutId(Components.Pointer),
            )
        },
    ) { measurables, constraints ->
        val mapPlaceable = run {
            val measurable = measurables.fastFirst { it.layoutId == Components.Map }
            // SV map size should satisfy specifications from the passed 'modifier'
            measurable.measure(constraints)
        }
        val pointerPlaceable = run {
            val measurable = measurables.fastFirst { it.layoutId == Components.Pointer }
            // pointer size may be smaller than the SV map
            val constraints = constraints.copy(minWidth = 0, minHeight = 0)
            measurable.measure(constraints)
        }
        val pointerPosition = run {
            val mapSize = IntSize(mapPlaceable.width, mapPlaceable.height)
            val centerOfPointerSize = IntSize(pointerPlaceable.width, pointerPlaceable.height).center
            val posOfPointerCenter = Offset(
                x = mapSize.width * sv.saturation,
                y = mapSize.height * (1f - sv.value),
            )
            val posOfPointerTopLeft = IntOffset(
                x = (posOfPointerCenter.x - centerOfPointerSize.x).roundToInt(),
                y = (posOfPointerCenter.y - centerOfPointerSize.y).roundToInt(),
            )
            posOfPointerTopLeft
        }
        layout(width = mapPlaceable.width, height = mapPlaceable.height) {
            mapPlaceable.placeRelative(x = 0, y = 0)
            pointerPlaceable.placeRelative(position = pointerPosition)
        }
    }
}

/**
 * The value of the 'hue' in the HSV color space.
 */
@JvmInline
internal value class HueValue(val value: Float) {
    init {
        require(value in HsvHueRange)
    }
}

internal fun HueValue(color: DomainColor.Hsv): HueValue =
    HueValue(value = color.hue)

/**
 * The value of the 'saturation' and 'value' in the HSV color space.
 */
internal data class SaturationAndValue(
    val saturation: Float,
    val value: Float,
) {
    init {
        require(saturation in HsvSaturationRange)
        require(value in HsvValueRange)
    }
}

internal fun SaturationAndValue(color: DomainColor.Hsv): SaturationAndValue =
    SaturationAndValue(saturation = color.saturation, value = color.value)

/**
 * The enumeration of all the UI components that are present in [SaturationAndValuePicker].
 */
private enum class Components {
    Map, Pointer;
}

@Composable
private fun SaturationAndValueMap(
    hue: HueValue,
    modifier: Modifier = Modifier,
) {
    val maxSvColor = remember(hue) {
        HsvColor(components = floatArrayOf(/*h*/ hue.value, /*s*/ 1f, /*v*/ 1f))
    }
    Canvas(
        modifier = modifier.aspectRatio(1f / 1f),
    ) {
        val saturationGradient = Brush.horizontalGradient(
            colors = listOf(ComposeColor.White, maxSvColor),
        )
        drawRect(saturationGradient)
        val valueGradient = Brush.verticalGradient(
            colors = listOf(ComposeColor.Transparent, ComposeColor.Black),
        )
        drawRect(valueGradient)
    }
}

@Composable
private fun Pointer(
    modifier: Modifier = Modifier,
) {
    val radius = 8.dp
    val strokeWidth = 3.dp
    // stroke is drawn on top of the circumference in the middle,
    // so that half of the stroke width is outside the circle and the other half is inside
    val totalSize = (radius * 2) + strokeWidth
    Canvas(
        modifier = modifier.size(totalSize),
    ) {
        val radius = radius.toPx()
        val strokeWidth = strokeWidth.toPx()
        drawCircle(
            color = ComposeColor.White,
            radius = radius,
            center = this.size.center,
            style = Stroke(width = strokeWidth),
        )
    }
}

@Composable
@Preview
private fun Preview() {
    TheColorTheme {
        var sv by remember {
            val value = SaturationAndValue(saturation = 0.59f, value = 0.99f)
            mutableStateOf(value)
        }
        SaturationAndValuePicker(
            modifier = Modifier
                .padding(all = 16.dp)
                .aspectRatio(1f / 1f),
            hue = HueValue(259f),
            sv = sv,
            onChange = { newSv ->
                sv = newSv
            },
        )
    }
}