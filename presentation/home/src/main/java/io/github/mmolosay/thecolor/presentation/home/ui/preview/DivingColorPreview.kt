package io.github.mmolosay.thecolor.presentation.home.ui.preview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.compose.drawIf
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.ui.ColorCenterFocalPointBottomOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview as AnimState

/**
 * Animates 'Color Preview' position (dive) and manipulates its data to display [content]
 * in an appropriate (relative to 'Home's animation) state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun DivingColorPreview(
    flowOfPositionAnimDest: StateFlow<AnimState.Position>,
    onPositionReached: (reached: AnimState.Position) -> Unit,
    stateOfContainerViewportHeight: State<Int?>,
    stateOfContainerPosInRoot: State<Offset?>,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current

    var posInContainer by remember { mutableStateOf<Offset?>(null) }
    var size by remember { mutableStateOf<IntSize?>(null) }

    val verticalOffsetParams by produceState<VerticalOffset.Params?>(
        initialValue = null,
        /*keys*/ stateOfContainerViewportHeight.value, size, posInContainer,
    ) {
        value = VerticalOffset.paramsOrNull(
            containerViewportHeight = stateOfContainerViewportHeight.value,
            previewSize = size,
            previewPosInContainer = posInContainer,
        )
    }
    val offsetAnimatable by produceState<Animatable<Int, AnimationVector1D>?>(
        initialValue = null,
        /*keys*/ verticalOffsetParams,
    ) {
        if (value != null) return@produceState // already initialized
        val params = verticalOffsetParams ?: return@produceState
        val positionAnimDest = flowOfPositionAnimDest.value
        value = Animatable(
            initialValue = VerticalOffset.calc(positionAnimDest, params, density),
            typeConverter = Int.VectorConverter,
            visibilityThreshold = Int.VisibilityThreshold,
            label = "dive",
        )
    }

    val isReadyToBeDrawn = (offsetAnimatable != null)
    Box(
        modifier = Modifier
            .drawIf(isReadyToBeDrawn)
            .onGloballyPositioned { coordinates ->
                size = coordinates.size
            }
            // 'posInContainer' should be calculated before applying 'dive' animation offset (modifier)
            .onGloballyPositioned l@{ coordinates ->
                val containerPosInRoot = stateOfContainerPosInRoot.value
                if (containerPosInRoot == null) return@l
                val ownPosInRoot = coordinates.positionInRoot()
                posInContainer = ownPosInRoot - containerPosInRoot
            }
            .offset {
                IntOffset(x = 0, y = offsetAnimatable?.value ?: 0)
            },
    ) {
        content()
    }

    LaunchedEffect(Unit) {
        flowOfPositionAnimDest.collectLatest collect@{ animDest ->
            val offsetAnimatable = offsetAnimatable ?: return@collect
            val targetValue = run {
                val params = verticalOffsetParams ?: return@collect
                VerticalOffset.calc(animDest, params, density)
            }
            if (offsetAnimatable.value == targetValue) {
                onPositionReached(animDest)
                return@collect // already in target state
            }
            val animationSpec: AnimationSpec<Int> = when (animDest) {
                AnimState.Position.NotDived ->
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    )
                AnimState.Position.Dived ->
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
            }
            offsetAnimatable.animateTo(
                targetValue = targetValue,
                animationSpec = animationSpec,
            )
            onPositionReached(animDest)
        }
    }
}

private object VerticalOffset {

    fun calc(
        position: AnimState.Position,
        params: Params,
        density: Density,
    ): Int =
        when (position) {
            AnimState.Position.NotDived ->
                0
            AnimState.Position.Dived -> {
                val focalPointBottomOffset =
                    with(density) { ColorCenterFocalPointBottomOffset.toPx() }
                val diveTargetPointInContainer =
                    params.containerViewportHeight - (params.previewSize.height / 2) - focalPointBottomOffset
                val dive = diveTargetPointInContainer - params.previewPosInContainer.y
                dive.toInt().coerceAtLeast(0)
            }
        }


    data class Params(
        val containerViewportHeight: Int,
        val previewSize: IntSize,
        val previewPosInContainer: Offset,
    )

    fun paramsOrNull(
        containerViewportHeight: Int?,
        previewSize: IntSize?,
        previewPosInContainer: Offset?,
    ): Params? {
        return Params(
            containerViewportHeight = containerViewportHeight ?: return null,
            previewSize = previewSize ?: return null,
            previewPosInContainer = previewPosInContainer ?: return null,
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF_FFFFFF,
)
@Composable
private fun Preview() {
    TheColorTheme {
        DivingColorPreview(
            flowOfPositionAnimDest = remember { MutableStateFlow(AnimState.Position.NotDived) },
            onPositionReached = {},
            stateOfContainerViewportHeight = remember { mutableIntStateOf(400) },
            stateOfContainerPosInRoot = remember { mutableStateOf(Offset.Zero) },
        ) {
            Placeholder(
                Modifier.size(64.dp),
            ) {
                Text(
                    text = "Color Preview",
                    autoSize = TextAutoSize.StepBased(),
                )
            }
        }
    }
}