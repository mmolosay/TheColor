package io.github.mmolosay.thecolor.presentation.impl

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import kotlinx.coroutines.launch
import kotlin.math.hypot

/**
 * A container for the [content] to which circular reveal animation is applied to.
 * Only composes [content] if it would be visible considering current animation state.
 * [content] must have [Modifier.clipCircle] applied to it.
 */
@Composable
fun CircularReveal(
    animProgress: Float,
    content: @Composable () -> Unit,
) {
    val updatedAnimProgress by rememberUpdatedState(animProgress) // 'derivedStateOf' reads State<T>
    val composeContent by remember {
        derivedStateOf {
            val isFullyCollapsed = (updatedAnimProgress == 0f)
            !isFullyCollapsed
        }
    }
    if (composeContent) {
        content()
    }
}

/**
 * Clips the content to a circle shape.
 *
 * @param center position of the center of the clipping circle using size of the element
 * this modifier is applied to.
 * @param radius length of the clipping circle's radius.
 */
fun Modifier.clipCircle(
    center: (Size) -> Offset,
    radius: RadiusProvider,
): Modifier =
    drawWithCache {
        // TODO: for some reason, re-allocates and re-executes values that are meant to be cached
        //  on every animation frame; investigate
        val path = Path()
        val center = center(this.size)
        val radiusOfCoveringCircle = center.radiusOfCoveringCircle(this.size.toRect())

        onDrawWithContent {
            path.rewind()
            val circleRect = Rect(
                center = center,
                radius = radius(elementSize = this.size, minCoverRadius = radiusOfCoveringCircle),
            )
            path.addOval(circleRect)

            clipPath(path) {
                this@onDrawWithContent.drawContent()
            }
        }
    }

fun interface RadiusProvider {
    /**
     * @param elementSize size of the UI element the [clipCircle] modifier is applied to.
     * @param minCoverRadius radius to fully cover the UI element. See [radiusOfCoveringCircle].
     */
    operator fun invoke(elementSize: Size, minCoverRadius: Float): Float
}

/**
 * Calculates a radius of a smallest circle that will fully cover given [rect].
 * The center of the circle is at receiver [Offset], and [rect] is placed at [Offset.Zero].
 */
private fun Offset.radiusOfCoveringCircle(rect: Rect): Float {
    val center = this
    val corners = listOf(
        rect.topLeft,
        rect.topRight,
        rect.bottomRight,
        rect.bottomLeft,
    )
    val distanceToCorners = corners.map { corner ->
        hypot(corner.x - center.x, corner.y - center.y)
    }
    val distanceToFurthestCorner = distanceToCorners.max()
    return distanceToFurthestCorner
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF_888888,
)
@Composable
private fun Preview() {
    TheColorTheme {
        val progressAnimatable = remember {
            Animatable(initialValue = 0f)
        }
        val coroutineScope = rememberCoroutineScope()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipCircle(
                        center = { size -> size.center },
                        radius = RadiusProvider { size, minCoverRadius ->
                            minCoverRadius * progressAnimatable.value
                        },
                    )
                    .background(Color.Blue),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "This is some content for a preview",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayLarge,
                )
            }

            Row {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            progressAnimatable.animateTo(targetValue = 1f)
                        }
                    },
                ) {
                    Text(text = "Expand")
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            progressAnimatable.animateTo(targetValue = 0f)
                        }
                    },
                ) {
                    Text(text = "Collapse")
                }
            }
        }
    }
}