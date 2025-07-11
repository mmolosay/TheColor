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
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.toDpOffset
import io.github.mmolosay.thecolor.presentation.impl.toDpSize
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimControllerImpl
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import io.github.mmolosay.thecolor.presentation.preview.toUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview as AnimState

/**
 * Animates 'Color Preview' position (dive) and manipulates its data to display 'Color Preview'
 * in an appropriate state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun AnimatedColorPreview(
    colorPreview: ColorPreviewWithDependencies,
    flowOfPositionAnimDest: StateFlow<AnimState.Position>,
    flowOfVisibilityAnimDest: StateFlow<AnimState.Visibility>,
    onPositionReached: (reached: AnimState.Position) -> Unit,
    onVisibilityReached: (reached: AnimState.Visibility) -> Unit,
    containerViewportHeight: Dp?,
    containerPosInRoot: DpOffset?,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var posInContainer by remember { mutableStateOf<DpOffset?>(null) }
    var size by remember { mutableStateOf<DpSize?>(null) }

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
        val positionAnimDest = flowOfPositionAnimDest.value
        value = Animatable(
            initialValue = VerticalOffset.calc(positionAnimDest, params),
            typeConverter = Dp.VectorConverter,
            visibilityThreshold = Dp.VisibilityThreshold,
            label = "dive",
        )
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                size = coordinates.size.toDpSize(density)
            }
            // 'posInContainer' should be calculated before applying 'dive' animation offset (modifier)
            .onGloballyPositioned l@{ coordinates ->
                if (containerPosInRoot == null) return@l
                val ownPosInRoot = coordinates.positionInRoot().toDpOffset(density)
                posInContainer = ownPosInRoot - containerPosInRoot
            }
            .run applyDiveAnimOffset@{
                val animatable = offsetAnimatable
                if (animatable != null) {
                    offset { IntOffset(x = 0, y = animatable.value.roundToPx()) }
                } else this
            },
    ) {
        val flowOfAnimatedUiState = remember {
            FlowOfAnimatedUiState(
                flowOfOriginalData = colorPreview.viewModel.dataFlow.filterNotNull(),
                flowOfVisibilityAnimDest = flowOfVisibilityAnimDest,
                coroutineScope = coroutineScope,
            )
        }
        val animController by produceState<ColorPreviewAnimController?>(initialValue = null) {
            val uiState = flowOfAnimatedUiState.filterNotNull().first()
            value = ColorPreviewAnimControllerImpl(uiState)
        }
        LaunchedEffect(Unit) {
            flowOfAnimatedUiState.filterNotNull().collect { uiState ->
                animController?.onNewUiState(uiState)
            }
        }
        if (animController != null) {
            colorPreview.composable.invoke(
                animController = animController!!,
                onUiStateReached = { reachedUiState ->
                    val reachedAnimState = reachedUiState.toAnimState()
                    onVisibilityReached(reachedAnimState)
                },
            )
        }
    }

    // TODO: position is being animated here, but visibility (collapse & expand) in ColorPreview() Composable itself
    //  Why such a separation?
    LaunchedEffect(Unit) {
        flowOfPositionAnimDest.collectLatest collect@{ animDest ->
            val offsetAnimatable = offsetAnimatable ?: return@collect
            val targetValue = kotlin.run {
                val params = verticalOffsetParams ?: return@collect
                VerticalOffset.calc(animDest, params)
            }
            if (offsetAnimatable.value == targetValue) {
                onPositionReached(animDest)
                return@collect // already in target state
            }
            val animationSpec: AnimationSpec<Dp> = when (animDest) {
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
    ): Dp =
        when (position) {
            AnimState.Position.NotDived ->
                0.dp
            AnimState.Position.Dived -> {
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
    flowOfOriginalData: Flow<ColorPreviewData>,
    flowOfVisibilityAnimDest: StateFlow<AnimState.Visibility>,
    coroutineScope: CoroutineScope,
): StateFlow<ColorPreviewUiState?> {
    val pendingUiStates = mutableListOf<ColorPreviewUiState>()
    var pendingAnimDest: AnimState.Visibility? = null
    val windowBetweenOriginalDataAndAnimDest = 24.64.milliseconds * 2 // see benchmark comment below
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
        flowOfVisibilityAnimDest.collect { animDest ->
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
                val currentAnimDest = flowOfVisibilityAnimDest.value
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

private fun ColorPreviewUiState.toAnimState(): AnimState.Visibility =
    when (this) {
        is ColorPreviewUiState.Hidden -> AnimState.Visibility.Hidden
        is ColorPreviewUiState.Visible -> AnimState.Visibility.Visible
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
            flowOfPositionAnimDest = remember { MutableStateFlow(AnimState.Position.NotDived) },
            flowOfVisibilityAnimDest = remember { MutableStateFlow(AnimState.Visibility.Visible) },
            onPositionReached = {},
            onVisibilityReached = {},
            containerViewportHeight = 400.dp,
            containerPosInRoot = DpOffset.Zero,
        )
    }
}