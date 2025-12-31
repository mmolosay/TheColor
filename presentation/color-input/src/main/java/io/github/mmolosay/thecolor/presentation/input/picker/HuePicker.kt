package io.github.mmolosay.thecolor.presentation.input.picker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.VerticalSlider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.picker.ColorUtils.HsvColor
import io.github.mmolosay.thecolor.presentation.input.picker.ColorUtils.HsvHueRange
import kotlin.math.nextDown
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HuePicker2(
    modifier: Modifier = Modifier,
    hue: HueValue,
    onChange: (HueValue) -> Unit,
    hueBarShape: Shape = RectangleShape,
) {
    @Suppress("NAME_SHADOWING") // intentional name shadowing to enforce using this wrapped lambda instead of the one passed in arguments
    val onChange by rememberUpdatedState(onChange) // preventive measure to avoid possible redraws due to new lambda
    val sliderState = rememberSliderState(
        value = hue.value,
        valueRange = with(HsvHueRange) { start..endExclusive.nextDown() },
    )
    val sliderColors = SliderDefaults.colors()
    VerticalSlider(
        modifier = modifier,
        state = sliderState,
        colors = sliderColors,
        track = {
            SliderDefaults.Track(
                colors = sliderColors,
                enabled = true,
                sliderState = sliderState,
                trackCornerSize = 4.dp,
            )
        },
        // TODO: use 'HueBar' for track and apply 'hueBarShape' to it
    )
    LaunchedEffect(sliderState) {
        snapshotFlow { sliderState.value }.collect { newHue ->
            onChange(HueValue(newHue))
        }
    }
}

@Composable
internal fun HuePicker(
    modifier: Modifier = Modifier,
    hue: HueValue,
    onChange: (HueValue) -> Unit,
    hueBarShape: Shape = RectangleShape,
) {
    val density = LocalDensity.current

    @Suppress("NAME_SHADOWING") // intentional name shadowing to enforce using this wrapped lambda instead of the one passed in arguments
    val onChange by rememberUpdatedState(onChange) // preventive measure to avoid possible redraws due to new lambda
    var hueBarSize: Size? by remember { mutableStateOf(null) }
    var pointerYPosition: Float? by remember { mutableStateOf(null) }
    LaunchedEffect(hueBarSize) {
        val hueBarSize = hueBarSize ?: return@LaunchedEffect
        val pointerYMult = hue.value / HsvHueRange.endExclusive.nextDown()
        val pointerWindowHeight = with(density) { PointerWindowHeight.toPx() }
        require(pointerYMult in 0f..1f)
        pointerYPosition = (hueBarSize.height - pointerWindowHeight) * pointerYMult
    }
    fun processAndUpdatePointerPosition(pointerRawYPos: Float) {
        val hueBarSize = hueBarSize ?: return // ignore if hasn't been measured yet
        val pointerWindowHeight = with(density) { PointerWindowHeight.toPx() }
        val coercedPosition = pointerRawYPos.coerceIn(0f..hueBarSize.height - pointerWindowHeight)
        pointerYPosition = coercedPosition
    }

    val pointerHue: HueValue by remember {
        derivedStateOf {
            val pointerYPosition = pointerYPosition ?: return@derivedStateOf hue
            val hueBarSize = hueBarSize ?: return@derivedStateOf hue
            val pointerYMult = pointerYPosition / hueBarSize.height
            require(pointerYMult in 0f..1f)
            val pointerHue = pointerYMult * HsvHueRange.endExclusive.nextDown()
            HueValue(pointerHue)
        }
    }
    Box(
        modifier = modifier,
    ) {
        HueBar(
            modifier = Modifier
                .padding(horizontal = PointerStrokeWidth, vertical = PointerStrokeWidth)
                .clip(hueBarShape)
                .onSizeChanged {
                    hueBarSize = it.toSize()
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { position ->
                            processAndUpdatePointerPosition(position.y)
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            processAndUpdatePointerPosition(change.position.y)
                        }
                    )
                },
        )
        Pointer(
            modifier = Modifier.offset {
                val pointerYPosition = pointerYPosition ?: return@offset IntOffset.Zero
                IntOffset(x = 0, y = pointerYPosition.roundToInt())
            },
        )
    }

    LaunchedEffect(pointerHue) {
        onChange(pointerHue)
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
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(PointerHeight)
    ) {
        val strokeWidth = PointerStrokeWidth.toPx()
        val cornerRadius = PointerCornerRadius.toPx()
        drawRoundRect(
            color = Color.White.copy(alpha = 0.8f), // TODO: remove alpha
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

private val PointerStrokeWidth = 3.dp
private val PointerCornerRadius = 3.dp
private val PointerWindowHeight = 4.dp
private val PointerHeight = PointerStrokeWidth + PointerWindowHeight + PointerStrokeWidth

@Preview
@Composable
private fun Preview_Type_2() {
    TheColorTheme {
        HuePicker2(
            modifier = Modifier
                .width(32.dp)
                .height(192.dp),
            hue = HueValue.Min,
            onChange = {},
        )
    }
}

@Preview
@Composable
private fun Preview_Min() {
    TheColorTheme {
        HuePicker(
            modifier = Modifier
                .width(32.dp)
                .height(192.dp),
            hue = HueValue.Min,
            onChange = {},
        )
    }
}

@Preview
@Composable
private fun Preview_Max() {
    TheColorTheme {
        HuePicker(
            modifier = Modifier
                .width(32.dp)
                .height(192.dp),
            hue = HueValue.Max,
            onChange = {},
        )
    }
}