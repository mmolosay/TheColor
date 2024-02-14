package io.github.mmolosay.thecolor.presentation.center

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

object ColorCenterShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            val height = size.width * 0.15f
            val controlPointX = size.width * 0.20f
            moveTo(x = 0f, y = height)
            quadraticBezierTo(
                x1 = 0 + controlPointX, y1 = 0f,
                x2 = size.width / 2, y2 = 0f,
            )
            quadraticBezierTo(
                x1 = size.width - controlPointX, y1 = 0f,
                x2 = size.width, y2 = height,
            )
            lineTo(x = size.width, y = size.height)
            lineTo(x = 0f, y = size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview1() {
    Preview(modifier = Modifier.size(width = 100.dp, height = 300.dp))
}

@Preview(showBackground = true)
@Composable
private fun Preview2() {
    Preview(modifier = Modifier.size(width = 200.dp, height = 300.dp))
}

@Preview(showBackground = true)
@Composable
private fun Preview3() {
    Preview(modifier = Modifier.size(width = 300.dp, height = 300.dp))
}

@Composable
private fun Preview(modifier: Modifier) {
    TheColorTheme {
        Box(
            modifier = modifier
                .graphicsLayer {
                    clip = true
                    shape = ColorCenterShape
                }
                .background(Color.DarkGray)
        ) {
            Text(text = "Hi, this is a preview for custom shape.")
        }
    }
}