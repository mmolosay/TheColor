package io.github.mmolosay.thecolor.presentation.input.picker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.mmolosay.thecolor.presentation.common.compose.drawIf
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.picker.ColorUtils.HsvColor

@Composable
internal fun SaturationAndValuePicker(
    modifier: Modifier = Modifier,
    hue: HueSource,
    saturationAndValue: SaturationAndValue,
    onChange: (SaturationAndValue) -> Unit,
) {
    val pureHueColor = remember(hue) {
        hue.toColor()
    }
    @Suppress("NAME_SHADOWING") // intentional name shadowing to enforce using this wrapped lambda instead of the one passed in arguments
    val onChange by rememberUpdatedState(onChange) // preventive measure to avoid possible redraws due to new lambda
    var size: Size? by remember { mutableStateOf(null) }
    var pointerPosition: PointerPosition by remember {
        val value = PointerPosition.SvCoordinates(saturationAndValue)
        mutableStateOf(value)
    }
    val pointerSv: SaturationAndValue? by remember {
        derivedStateOf {
            val size = size ?: return@derivedStateOf null
            val pointerOffset = pointerPosition.toOffset(size)
            val saturation = pointerOffset.x / size.width
            val value = pointerOffset.y / size.height
            SaturationAndValue(saturation, value)
        }
    }
    fun processAndUpdatePointerPosition(rawPosition: Offset) {
        val size = size ?: return // ignore if hasn't been measured yet
        val coercedPosition = Offset(
            x = rawPosition.x.coerceIn(0f, size.width.toFloat()),
            y = rawPosition.y.coerceIn(0f, size.height.toFloat())
        )
        pointerPosition = PointerPosition.PixelCoordinates(coercedPosition)
    }
    Canvas(
        modifier = modifier
            .drawIf(size != null)
            .size(96.dp)
            .onSizeChanged {
                size = it.toSize()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { position ->
                        processAndUpdatePointerPosition(position)
                    },
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        processAndUpdatePointerPosition(change.position)
                    },
                )
            },
    ) {
        val saturationGradient = Brush.horizontalGradient(
            colors = listOf(Color.White, pureHueColor),
        )
        drawRect(saturationGradient)
        val valueGradient = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black),
        )
        drawRect(valueGradient)
        drawPointer(position = pointerPosition.toOffset(this.size))
    }

    LaunchedEffect(pointerSv) {
        val pointerSv = pointerSv ?: return@LaunchedEffect
        onChange(pointerSv)
    }
}

internal sealed interface HueSource {
    data class FromColor(val color: Color) : HueSource
    data class FromHsv(/*0f<=value<360f*/ val value: Float) : HueSource
}

private sealed interface PointerPosition {
    data class SvCoordinates(val sv: SaturationAndValue) : PointerPosition
    data class PixelCoordinates(val offset: Offset) : PointerPosition
}

private fun PointerPosition.toOffset(viewportSize: Size): Offset =
    when (this) {
        is PointerPosition.PixelCoordinates -> this.offset
        is PointerPosition.SvCoordinates -> Offset(
            x = viewportSize.width * this.sv.saturation,
            y = viewportSize.height * (1f - this.sv.value),
        )
    }

private fun DrawScope.drawPointer(
    position: Offset,
) {
    val radius = 8.dp.toPx()
    val strokeWidth = 3.dp.toPx()
    drawCircle(
        color = Color.White,
        radius = radius,
        center = position,
        style = Stroke(width = strokeWidth),
    )
}

internal data class SaturationAndValue(
    val saturation: Float,
    val value: Float,
) {
    init {
        require(saturation in 0f..1f)
        require(value in 0f..1f)
    }
}

private fun HueSource.toColor(): Color =
    when (this) {
        is HueSource.FromColor -> this.color.toPureHue()
        is HueSource.FromHsv -> HsvColor(components = floatArrayOf(/*h*/ this.value, /*s*/ 1f, /*v*/ 1f))
    }

private fun Color.toPureHue(): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[1] = 1f // max saturation
    hsv[2] = 1f // max value, aka full brightness
    return HsvColor(components = hsv)
}

@Composable
@Preview
private fun Preview() {
    TheColorTheme {
        SaturationAndValuePicker(
            modifier = Modifier.aspectRatio(1f / 1f),
            hue = HueSource.FromColor(Color.Red),
            saturationAndValue = SaturationAndValue(saturation = 0.5f, value = 0.5f),
            onChange = {},
        )
    }
}