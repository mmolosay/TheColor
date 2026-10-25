package io.github.mmolosay.thecolor.presentation.common.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

fun Modifier.hazardStripes(
    color: Color = Color.Yellow,
    width: Dp = 8.dp,
): Modifier =
    drawBehind {
        val stripePx = width.toPx()
        val gapPx = width.toPx()
        val step = stripePx + gapPx
        val reach = hypot(size.width, size.height) // enough to cover the box once rotated

        clipRect { // trim the rotated stripes back to the component's bounds
            rotate(degrees = 45f) {
                var x = center.x - reach
                while (x < center.x + reach) {
                    drawRect(
                        color = color,
                        topLeft = Offset(x, center.y - reach),
                        size = Size(stripePx, reach * 2),
                    )
                    x += step
                }
            }
        }
    }

@Preview
@Composable
private fun Preview() {
    Box(
        Modifier
            .width(160.dp)
            .height(90.dp)
            .background(Color.White)
            .hazardStripes()
    )
}