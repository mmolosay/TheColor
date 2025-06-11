package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.annotation.VisibleForTesting
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
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import io.github.mmolosay.thecolor.presentation.preview.toUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview as ColorPreviewAnimState

/**
 * Animates 'Color Preview' position (dive) and manipulates its data to display 'Color Preview'
 * in an appropriate state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun AnimatedColorPreview(
    colorPreview: ColorPreviewWithDependencies,
    animDest: ColorPreviewAnimState,
    onPositionAnimDestReached: (dest: ColorPreviewAnimState.Position) -> Unit,
    onVisibilityAnimDestReached: (dest: ColorPreviewAnimState.Visibility) -> Unit,
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
        val flowOfAnimatedUiState = remember {
            FlowOfAnimatedUiState(
                flowOfOriginalData = colorPreview.viewModel.dataFlow,
                flowOfAnimDest = flowOfAnimDest,
                coroutineScope = coroutineScope,
            )
        }
        val uiState = flowOfAnimatedUiState.collectAsStateWithLifecycle().value
        if (uiState != null) {
            colorPreview.composable.invoke(
                uiState = uiState,
                onAnimationFinished = { uiState ->
                    val animState = uiState.toAnimState()
                    onVisibilityAnimDestReached(animState)
                },
            )
        }
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
            ColorPreviewAnimState.Position.NotDived ->
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                )
            ColorPreviewAnimState.Position.Dived ->
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
        }
        offsetAnimatable.animateTo(
            targetValue = targetValue,
            animationSpec = animationSpec,
        )
        onPositionAnimDestReached(animDestPosition)
    }
}

private object VerticalOffset {

    fun calc(
        position: ColorPreviewAnimState.Position,
        params: Params,
    ): Dp =
        when (position) {
            ColorPreviewAnimState.Position.NotDived ->
                0.dp
            ColorPreviewAnimState.Position.Dived -> {
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

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun FlowOfAnimatedUiState(
    flowOfOriginalData: StateFlow<ColorPreviewData>,
    flowOfAnimDest: StateFlow<ColorPreviewAnimState>,
    coroutineScope: CoroutineScope,
): StateFlow<ColorPreviewUiState?> {
    val pendingUiStates = mutableListOf<ColorPreviewUiState>()
    var pendingAnimDest: ColorPreviewAnimState.Visibility? = null
    val windowBetweenOriginalDataAndAnimDest = 24.64.milliseconds * 2 // see benchmark comment below

    val flowOfAnimDest = flowOfAnimDest.map { it.visibility }.stateIn(
        scope = coroutineScope, started = SharingStarted.WhileSubscribed(),
        initialValue = flowOfAnimDest.value.visibility,
    )
    val flowOfAnimatedUiState = MutableStateFlow<ColorPreviewUiState?>(null)

    fun trySatisfyPendingAnimDest(): Boolean {
        if (pendingAnimDest == null) return false
        if (pendingUiStates.isEmpty()) return false
        val pendingUiStatesToAnimStates =
            pendingUiStates
                .reversed() // newest first
                .map { uiState -> uiState to uiState.toAnimState() }
        val match =
            pendingUiStatesToAnimStates.firstOrNull { (uiState, animState) ->
                animState == pendingAnimDest
            }
        if (match != null) {
            pendingUiStates.clear() // only remove uiStates that were before the match?
            pendingAnimDest = null // satisfied and cleared
            flowOfAnimatedUiState.value = match.first // matched 'uiState'
            return true
        }
        return false
    }
    /*
     * Most of the times, new original data will be emitted and collected first,
     * and new anim dest (if any) will be emitted and collected second.
     * Elapsed between original data and anim dest emissions:
     * Mean average: 13.65ms
     * Median: 13.61ms
     * Range: 3.18ms - 24.64ms
     */
    coroutineScope.launch {
        flowOfAnimDest.collect { animDest ->
            pendingAnimDest = animDest
            trySatisfyPendingAnimDest()
        }
    }
    coroutineScope.launch {
        flowOfOriginalData.map { data -> data.toUiState() }.collect { uiState ->
            pendingUiStates += uiState
            val wasSatisfied = trySatisfyPendingAnimDest()
            if (wasSatisfied) return@collect
            delay(windowBetweenOriginalDataAndAnimDest) // allow new anim dest to arrive
            if (pendingAnimDest == null) {
                // emit this uiState if it satisfies current, already satisfied anim dest
                val animState = uiState.toAnimState()
                val currentAnimDest = flowOfAnimDest.value
                if (animState == currentAnimDest) {
                    flowOfAnimatedUiState.value = uiState
                    // remove this uiState as fulfilled
                    pendingUiStates.lastOrNull()?.let { lastAdded ->
                        if (lastAdded == uiState) pendingUiStates.removeLastOrNull()
                    }
                }
            }
        }
    }
    return flowOfAnimatedUiState.asStateFlow()
}

private fun ColorPreviewUiState.toAnimState(): ColorPreviewAnimState.Visibility =
    when (this) {
        is ColorPreviewUiState.Hidden -> ColorPreviewAnimState.Visibility.Hidden
        is ColorPreviewUiState.Visible -> ColorPreviewAnimState.Visibility.Visible
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
                NoopColorPreviewWithDependencies { _, _ ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(shape = CircleShape)
                            .background(Color.DarkGray)
                    )
                }
            },
            animDest = ColorPreviewAnimState(
                position = ColorPreviewAnimState.Position.NotDived,
                visibility = ColorPreviewAnimState.Visibility.Visible,
            ),
            onPositionAnimDestReached = {},
            onVisibilityAnimDestReached = {},
            containerViewportHeight = 400.dp,
            containerPosInRoot = DpOffset.Zero,
        )
    }
}