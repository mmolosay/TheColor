package io.github.mmolosay.thecolor.presentation.home

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.HomeUiData.ColorPreviewState
import io.github.mmolosay.thecolor.presentation.impl.toDpOffset
import io.github.mmolosay.thecolor.presentation.impl.toDpSize

@Composable
internal fun AnimatedColorPreview(
    colorPreview: @Composable () -> Unit,
    state: ColorPreviewState,
    containerSize: DpSize?,
    containerPositionInRoot: DpOffset?,
) {
    val density = LocalDensity.current
    var initialPositionInContainer by remember { mutableStateOf<DpOffset?>(null) }
    var size by remember { mutableStateOf<DpSize?>(null) }
    val animTransition = updateTransition(
        targetState = state,
        label = "master transition",
    )
    val diveState = animTransition.animateDp(
        label = "dive",
    ) { targetState ->
        targetState.calcAnimationDive(
            containerSize = containerSize,
            previewSize = size,
            previewPositionInContainer = initialPositionInContainer,
        )
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(x = 0, y = diveState.value.roundToPx()) }
            .onGloballyPositioned { coordinates ->
                if (size != null && initialPositionInContainer != null) return@onGloballyPositioned
                size = coordinates.size.toDpSize(density)
                if (containerPositionInRoot != null) {
                    val selfPositionInRoot = coordinates
                        .positionInRoot()
                        .toDpOffset(density)
                    initialPositionInContainer = selfPositionInRoot - containerPositionInRoot
                }
            },
    ) {
        colorPreview()
    }
}

@Stable
private fun ColorPreviewState.calcAnimationDive(
    containerSize: DpSize?,
    previewSize: DpSize?,
    previewPositionInContainer: DpOffset?,
): Dp {
    when (this) {
        ColorPreviewState.Default -> return 0.dp
        ColorPreviewState.Submitted -> {
            containerSize ?: return 0.dp
            previewSize ?: return 0.dp
            previewPositionInContainer ?: return 0.dp
            val offsetFromContainerBottom = previewSize.height
            val diveTargetPointInContainer =
                containerSize.height - previewSize.height - offsetFromContainerBottom
            val dive = diveTargetPointInContainer - previewPositionInContainer.y
            return dive.coerceAtLeast(0.dp)
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF_FFFFFF,
)
@Composable
private fun Preview() {
    TheColorTheme {
        AnimatedColorPreview(
            colorPreview = {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(shape = CircleShape)
                        .background(Color.DarkGray)
                )
            },
            state = ColorPreviewState.Default,
            containerSize = DpSize(width = 150.dp, height = 400.dp),
            containerPositionInRoot = DpOffset.Zero,
        )
    }
}