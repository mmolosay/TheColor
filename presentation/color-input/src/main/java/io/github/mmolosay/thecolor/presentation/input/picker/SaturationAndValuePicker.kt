package io.github.mmolosay.thecolor.presentation.input.picker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.mmolosay.thecolor.presentation.common.compose.toIntOffset
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.picker.ColorUtils.HsvColor
import io.github.mmolosay.thecolor.presentation.input.picker.ColorUtils.HsvHueRange
import io.github.mmolosay.thecolor.presentation.input.picker.ColorUtils.HsvSaturationRange
import io.github.mmolosay.thecolor.presentation.input.picker.ColorUtils.HsvValueRange
import kotlin.math.nextDown

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
    val pointerPosition: Offset? by produceState(
        initialValue = null,
        /*keys*/ mapSize, sv,
    ) {
        val mapSize = mapSize ?: return@produceState
        value = Offset(
            x = mapSize.width * sv.saturation,
            y = mapSize.height * (1f - sv.value),
        )
    }
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
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart,
    ) {
        SaturationAndValueMap(
            modifier = Modifier
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
        pointerPosition?.let { pointerPosition ->
            Pointer(
                position = pointerPosition,
            )
        }
    }
}

@JvmInline
internal value class HueValue(val value: Float) {
    init {
        require(value in HsvHueRange)
    }
    companion object {
        val Min by lazy { HueValue(HsvHueRange.start) }
        val Max by lazy { HueValue(HsvHueRange.endExclusive.nextDown()) }
    }
}

internal data class SaturationAndValue(
    val saturation: Float,
    val value: Float,
) {
    init {
        require(saturation in HsvSaturationRange)
        require(value in HsvValueRange)
    }
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
            colors = listOf(Color.White, maxSvColor),
        )
        drawRect(saturationGradient)
        val valueGradient = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black),
        )
        drawRect(valueGradient)
    }
}

@Composable
private fun Pointer(
    position: Offset,
) {
    val radius = 8.dp
    val strokeWidth = 3.dp
    val totalSize = (radius * 2) + strokeWidth
    Canvas(
        modifier = Modifier
            .offset {
                val radius = (totalSize.toPx() / 2)
                val offset = position - Offset(x = radius, y = radius)
                offset.toIntOffset()
            }
            .size(totalSize),
    ) {
        val radius = radius.toPx()
        val strokeWidth = strokeWidth.toPx()
        drawCircle(
            color = Color.White,
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