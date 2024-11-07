package io.github.mmolosay.thecolor.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun AnimatedColorCenter(
    colorCenter: @Composable () -> Unit,
) {
    NotInlineContainer(
        modifier = Modifier
            .circularReveal(radius = 80.dp), // TODO: doesn't work as it should, development suspended
    ) {
        colorCenter()
    }
}

@Composable
private fun NotInlineContainer(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
    ) {
        content()
    }
}

private fun Modifier.circularReveal(
    radius: Dp,
): Modifier =
    this
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithCache {

            onDrawWithContent {
                drawContent()
                drawCircle(
                    color = Color.Black,
                    radius = radius.toPx(),
//                    center = Offset(x = size.width / 2, y = 40.dp.toPx()),
//                    center = Offset.Zero,
                    blendMode = BlendMode.DstIn,
                )
            }
        }