package io.github.mmolosay.thecolor.presentation.input.picker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.picker.ColorUtils.HsvColor
import kotlin.math.roundToInt

@Composable
internal fun HuePicker(
    modifier: Modifier = Modifier,
    hueBarShape: Shape = RectangleShape,
) {
    var hueBarSize: Size? by remember { mutableStateOf(null) }
    var pointerYPosition: Float? by remember { mutableStateOf(null) }
    fun processAndUpdatePointerPosition(pointerRawYPos: Float) {
        val hueBarSize = hueBarSize ?: return // ignore if hasn't been measured yet
        val coercedPosition = pointerRawYPos.coerceIn(0f..hueBarSize.height)
        pointerYPosition = coercedPosition
    }
//    Slider()
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { position ->
                        processAndUpdatePointerPosition(position.y)
                    },
                )
            },
    ) {
        HueBar(
            modifier = Modifier
                .padding(horizontal = 3.dp, vertical = 3.dp)
                .onSizeChanged {
                    hueBarSize = it.toSize()
                }
                .clip(hueBarShape),
        )
        Pointer(
            modifier = Modifier.offset {
                val pointerYPosition = pointerYPosition ?: return@offset IntOffset.Zero
                IntOffset(x = 0, y = pointerYPosition.roundToInt())
            },
        )
    }
}

private fun makeListOfPureHues(): List<Color> {
    val totalHues = 360
    val maxHueValue = 360f
    val hueStep = (maxHueValue / totalHues)
    val hsvComponents =
        floatArrayOf(/*h*/ 0f, /*s*/ 1f, /*v*/ 1f) // reuse the same array to reduce allocations
    return List(totalHues) { index ->
        val hue = index * hueStep
        hsvComponents[0] = hue
        HsvColor(components = hsvComponents)
    }
}

@Composable
private fun HueBar(
    modifier: Modifier = Modifier,
) {
    val hues = remember {
        makeListOfPureHues()
    }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        val gradient = Brush.verticalGradient(hues)
        drawRect(gradient)
    }
}

@Composable
private fun Pointer(
    modifier: Modifier = Modifier,
) {
    val strokeWidth = 3.dp
    val cornerRadius = 3.dp
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        val strokeWidth = strokeWidth.toPx()
        val cornerRadius = cornerRadius.toPx()
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(
                x = 0f + (strokeWidth / 2),
                y = 0f + (strokeWidth / 2),
            ),
            size = Size(
                width = this.size.width - strokeWidth,
                height = this.size.height - strokeWidth,
            ),
            cornerRadius = CornerRadius(x = cornerRadius, y = cornerRadius),
            style = Stroke(
                width = strokeWidth,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TheColorTheme {
        HuePicker(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
        )
    }
}