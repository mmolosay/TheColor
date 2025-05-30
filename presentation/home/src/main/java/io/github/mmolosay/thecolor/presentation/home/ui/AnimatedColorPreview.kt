package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.toDpOffset
import io.github.mmolosay.thecolor.presentation.impl.toDpSize
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiStateController
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiStateFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import timber.log.Timber
import kotlin.time.measureTime

/**
 * Animates 'Color Preview' position (dive) and manipulates its data to display 'Color Preview'
 * in an appropriate state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun AnimatedColorPreview(
    colorPreview: ColorPreviewWithDependencies,
    animDest: HomeAnimState.ColorPreview,
    onAnimDestReached: (dest: HomeAnimState.ColorPreview.Position) -> Unit,
    containerViewportHeight: Dp?,
    containerPosInRoot: DpOffset?,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var posInContainer by remember { mutableStateOf<DpOffset?>(null) }
    var size by remember { mutableStateOf<DpSize?>(null) }

    val (animDestPosition, animDestVisibility) = animDest
    val flowOfAnimDest = remember { MutableStateFlow(animDest) }.also {
        it.value = animDest // LaunchedEffect introduces ~one frame delay, thus updating during composition
    }

    val verticalOffsetParams by produceState<VerticalOffset.Params?>(
        initialValue = null,
        /*keys*/ containerViewportHeight, size, posInContainer,
    ) {
        value = VerticalOffset.paramsOrNull(
            containerViewportHeight = containerViewportHeight,
            previewSize = size,
            previewPosInContainer = posInContainer,
        )
    }
    val offsetAnimatable by produceState<Animatable<Dp, AnimationVector1D>?>(
        initialValue = null,
        /*keys*/ verticalOffsetParams,
    ) {
        if (value != null) return@produceState // already initialized
        val params = verticalOffsetParams ?: return@produceState
        value = Animatable(
            initialValue = VerticalOffset.calc(animDestPosition, params),
            typeConverter = Dp.VectorConverter,
            visibilityThreshold = Dp.VisibilityThreshold,
            label = "dive",
        )
    }

    val controller = remember {
        val filter = ColorPreviewUiStateFilterImpl(
            flowOfAnimDest = flowOfAnimDest,
        )
        ColorPreviewUiStateController(
            coroutineScope = coroutineScope,
            dataFlow = colorPreview.viewModel.dataFlow,
            filter = filter,
        )
    }

    Box(
        modifier = Modifier
            .run {
                val animatable = offsetAnimatable
                if (animatable != null) {
                    offset { IntOffset(x = 0, y = animatable.value.roundToPx()) }
                } else this
            }
            .onGloballyPositioned { coordinates ->
                size = coordinates.size.toDpSize(density)
            }
            .onGloballyPositioned l@{ coordinates ->
                if (containerPosInRoot == null) return@l
                val ownPosInRoot = coordinates.positionInRoot().toDpOffset(density)
                posInContainer = ownPosInRoot - containerPosInRoot
            },
    ) {
        val uiState = controller.uiStateFlow.collectAsStateWithLifecycle().value
        colorPreview.composable.invoke(uiState)
    }

    // TODO: position is being animated here, but visibility (collapse & expand) in ColorPreview() Composable itself
    //  Why such a separation?
    LaunchedEffect(animDestPosition) {
        val offsetAnimatable = offsetAnimatable ?: return@LaunchedEffect
        val targetValue = kotlin.run {
            val params = verticalOffsetParams ?: return@LaunchedEffect
            VerticalOffset.calc(animDestPosition, params)
        }
        if (offsetAnimatable.value == targetValue) {
            return@LaunchedEffect // already in target state
        }
        val animationSpec: AnimationSpec<Dp> = when (animDestPosition) {
            HomeAnimState.ColorPreview.Position.NotDived ->
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                )
            HomeAnimState.ColorPreview.Position.Dived ->
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
        }
        offsetAnimatable.animateTo(
            targetValue = targetValue,
            animationSpec = animationSpec,
        )
        // animation has finished and we don't need to hold Visible uiState anymore
        controller.catchUp()
        onAnimDestReached(animDestPosition)
    }
}

private object VerticalOffset {

    fun calc(
        position: HomeAnimState.ColorPreview.Position,
        params: Params,
    ): Dp =
        when (position) {
            HomeAnimState.ColorPreview.Position.NotDived ->
                0.dp
            HomeAnimState.ColorPreview.Position.Dived -> {
                val diveTargetPointInContainer =
                    params.containerViewportHeight - (params.previewSize.height / 2) - ColorCenterFocalPointBottomOffset
                val dive = diveTargetPointInContainer - params.previewPosInContainer.y
                dive.coerceAtLeast(0.dp)
            }
        }


    data class Params(
        val containerViewportHeight: Dp,
        val previewSize: DpSize,
        val previewPosInContainer: DpOffset,
    )

    fun paramsOrNull(
        containerViewportHeight: Dp?,
        previewSize: DpSize?,
        previewPosInContainer: DpOffset?,
    ): Params? {
        return Params(
            containerViewportHeight = containerViewportHeight ?: return null,
            previewSize = previewSize ?: return null,
            previewPosInContainer = previewPosInContainer ?: return null,
        )
    }
}

/**
 * Skips (filters out) certain `uiState`s in order to retain previous
 * emission to be used in "exiting" animation of 'Color Preview'.
 */
private class ColorPreviewUiStateFilterImpl(
    private val flowOfAnimDest: StateFlow<HomeAnimState.ColorPreview>,
//    private val flowOfCurrentUiState: StateFlow<ColorPreviewUiState>, // TODO: implement and use in assert(), TDC:001
) : ColorPreviewUiStateFilter {

    override suspend fun submit(uiState: ColorPreviewUiState): Boolean {
        if (uiState is ColorPreviewUiState.Visible) {
            return true
        }
        assert(uiState is ColorPreviewUiState.Hidden) // the only type left excluding Visible
        if (flowOfAnimDest.value.position == HomeAnimState.ColorPreview.Position.NotDived) {
            return true
        }
        assert(flowOfAnimDest.value.position == HomeAnimState.ColorPreview.Position.Dived) // will soon change to 'NotDived'
        // TODO: metaprogramming; we should get information in the comment above from some code, not by knowing internal structure of HomeViewModel
        // implies that current 'UiState' on UI is 'Visible' // TODO: here, TDC:001
        val updatedAnimDest: HomeAnimState.ColorPreview
        val elapsed = measureTime {
            updatedAnimDest = flowOfAnimDest
                .drop(1) // replayed value of StateFlow
                .first()
        }
        // average 'elapsed' is 10-40 ms with peaks up 90+ ms
        Timber.i("'animDest' has changed to $updatedAnimDest in $elapsed after $uiState was emitted")
        return when (updatedAnimDest.position) {
            HomeAnimState.ColorPreview.Position.NotDived -> false // skip this 'uiState' thus keeping previous appearance to be used while Dived -> Initial animation plays
            HomeAnimState.ColorPreview.Position.Dived -> error("not possible")
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
            colorPreview = remember {
                NoopColorPreviewWithDependencies {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(shape = CircleShape)
                            .background(Color.DarkGray)
                    )
                }
            },
            animDest = HomeAnimState.ColorPreview(
                position = HomeAnimState.ColorPreview.Position.NotDived,
                visibility = HomeAnimState.ColorPreview.Visibility.Visible,
            ),
            onAnimDestReached = {},
            containerViewportHeight = 400.dp,
            containerPosInRoot = DpOffset.Zero,
        )
    }
}