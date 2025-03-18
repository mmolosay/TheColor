package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
    onAnimDestReached: (dest: HomeAnimState.ColorPreview) -> Unit,
    containerSize: DpSize?,
    containerPositionInRoot: DpOffset?,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var initialPositionInContainer by remember { mutableStateOf<DpOffset?>(null) }
    var size by remember { mutableStateOf<DpSize?>(null) }

    fun calcOffset() =
        animDest.calcVerticalOffset(
            containerSize = containerSize,
            previewSize = size,
            previewPositionInContainer = initialPositionInContainer,
        )
    val offsetAnimatable = remember {
        Animatable(
            initialValue = calcOffset(),
            typeConverter = Dp.VectorConverter,
            visibilityThreshold = Dp.VisibilityThreshold,
            label = "dive",
        )
    }

    val flowOfAnimDest = remember {
        MutableStateFlow(animDest)
    }
    // LaunchedEffect introduces ~one frame delay, thus updating during composition
    flowOfAnimDest.value = animDest

    val controller = remember {
        ColorPreviewUiStateController(
            coroutineScope = coroutineScope,
            dataFlow = colorPreview.viewModel.dataFlow,
        ).apply {
            val filter = ColorPreviewUiStateFilterImpl(
                flowOfAnimDest = flowOfAnimDest,
            )
            this.filter = filter
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(x = 0, y = offsetAnimatable.value.roundToPx()) }
            .onGloballyPositioned { coordinates ->
                size = coordinates.size.toDpSize(density)
            }
            .onGloballyPositioned l@{ coordinates ->
                if (initialPositionInContainer != null) return@l // already set
                if (containerPositionInRoot == null) return@l
                val selfPositionInRoot = coordinates.positionInRoot().toDpOffset(density)
                initialPositionInContainer = selfPositionInRoot - containerPositionInRoot
            },
    ) {
        val uiState = controller.uiStateFlow.collectAsStateWithLifecycle().value
        colorPreview.composable.invoke(uiState)
    }

    LaunchedEffect(animDest) {
        val animationSpec: AnimationSpec<Dp> = when (animDest) {
            HomeAnimState.ColorPreview.Initial ->
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                )
            HomeAnimState.ColorPreview.Dived ->
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
        }
        offsetAnimatable.animateTo(
            targetValue = calcOffset(),
            animationSpec = animationSpec,
        )
        // animation has finished and we don't need to hold Visible uiState anymore
        controller.catchUp()
        onAnimDestReached(animDest)
    }
}

@Stable
private fun HomeAnimState.ColorPreview.calcVerticalOffset(
    containerSize: DpSize?,
    previewSize: DpSize?,
    previewPositionInContainer: DpOffset?,
): Dp {
    when (this) {
        HomeAnimState.ColorPreview.Initial ->
            return 0.dp
        HomeAnimState.ColorPreview.Dived -> {
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

/**
 * Skips (filters out) certain `uiState`s in order to retain previous
 * emission to be used in "exiting" animation of 'Color Preview'.
 */
private class ColorPreviewUiStateFilterImpl(
    private val flowOfAnimDest: StateFlow<HomeAnimState.ColorPreview>,
) : ColorPreviewUiStateFilter {

    override suspend fun submit(uiState: ColorPreviewUiState): Boolean {
        if (uiState is ColorPreviewUiState.Visible) {
            return true
        }
        // 'uiState' is Hidden
        if (flowOfAnimDest.value == HomeAnimState.ColorPreview.Initial) {
            return true
        }
        // 'animDest' is 'Dived', but will soon change to 'Initial'
        // TODO: metaprogramming; we should get information in the comment above from some code, not by knowing internal structure of HomeViewModel
        // implies that current 'UiState' on UI is 'Visible'
        val updatedAnimDest: HomeAnimState.ColorPreview
        val elapsed = measureTime {
            updatedAnimDest = flowOfAnimDest
                .drop(1) // replayed value of StateFlow
                .first()
        }
        Timber.i("'animDest' has changed to $updatedAnimDest in $elapsed after $uiState was emitted")
        return when (updatedAnimDest) {
            HomeAnimState.ColorPreview.Initial -> false // skip this 'uiState' thus keeping previous appearance to be used while Dived -> Initial animation plays
            HomeAnimState.ColorPreview.Dived -> error("not possible")
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
            animDest = HomeAnimState.ColorPreview.Initial,
            onAnimDestReached = {},
            containerSize = DpSize(width = 150.dp, height = 400.dp),
            containerPositionInRoot = DpOffset.Zero,
        )
    }
}