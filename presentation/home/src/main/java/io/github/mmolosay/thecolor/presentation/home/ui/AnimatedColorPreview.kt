package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.animation.core.Animatable
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
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.hasColor
import io.github.mmolosay.thecolor.utils.doNothing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/** UI animation state of 'Color Preview' element. */
private enum class ColorPreviewAnimState {
    Initial, Dived;
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
internal fun AnimatedColorPreview(
    colorPreview: ColorPreviewWithDependencies,
    isColorProceededWith: Boolean,
    containerSize: DpSize?,
    containerPositionInRoot: DpOffset?,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var initialPositionInContainer by remember { mutableStateOf<DpOffset?>(null) }
    var size by remember { mutableStateOf<DpSize?>(null) }

    val animDest = when (isColorProceededWith) {
        true -> ColorPreviewAnimState.Dived
        false -> ColorPreviewAnimState.Initial
    }
    fun calcAnimDestDive() =
        animDest.calcAnimationDive(
            containerSize = containerSize,
            previewSize = size,
            previewPositionInContainer = initialPositionInContainer,
        )
    val diveAnimatable = remember {
        Animatable(
            initialValue = calcAnimDestDive(),
            typeConverter = Dp.VectorConverter,
            visibilityThreshold = Dp.VisibilityThreshold,
            label = "dive",
        )
    }

    val flowOfIsColorProceededWith = remember {
        MutableStateFlow(isColorProceededWith)
    }
    // LaunchedEffect introduces ~one frame delay, thus updating during composition
    flowOfIsColorProceededWith.value = isColorProceededWith

    val actualDataFlow = colorPreview.dataFlow
    val pacedDataFlow = remember<SharedFlow<ColorPreviewData>> {
        actualDataFlow
            .transformLatest { data ->
                if (data.hasColor) {
                    emit(data); return@transformLatest
                }
                // has no color
                if (flowOfIsColorProceededWith.value == false) {
                    emit(data); return@transformLatest
                }
                // 'isColorProceededWith' is true, we have to give it a window to change to false
                val updated = withTimeoutOrNull(100.milliseconds) {
                    val start = TimeSource.Monotonic.markNow()
                    val value = flowOfIsColorProceededWith
                        .drop(1) // replayed value of StateFlow
                        .first()
                    val elapsed = start.elapsedNow()
                    Timber.i("Updated \'isColorProceededWith\' has arrived in $elapsed")
                    return@withTimeoutOrNull value
                }
                when (updated) {
                    null -> emit(data) // 'isColorProceededWith' hasn't changed
                    false -> doNothing() // skip this 'data'
                    true -> error("not possible")
                }
            }
            .shareIn(
                scope = coroutineScope,
                started = SharingStarted.Eagerly,
                replay = 1,
            )
    }
    val mutablePacedDataFlow = remember<MutableStateFlow<ColorPreviewData>> {
        MutableStateFlow(value = actualDataFlow.value)
    }
    LaunchedEffect(Unit) {
        pacedDataFlow.collect(mutablePacedDataFlow)
    }
    val data = mutablePacedDataFlow.collectAsStateWithLifecycle().value

    Box(
        modifier = Modifier
            .offset { IntOffset(x = 0, y = diveAnimatable.value.roundToPx()) }
            .onGloballyPositioned { coordinates ->
                size = coordinates.size.toDpSize(density)
            }
            .onGloballyPositioned { coordinates ->
                if (initialPositionInContainer != null) return@onGloballyPositioned // already set
                if (containerPositionInRoot == null) return@onGloballyPositioned
                val selfPositionInRoot = coordinates.positionInRoot().toDpOffset(density)
                initialPositionInContainer = selfPositionInRoot - containerPositionInRoot
            },
    ) {
        colorPreview.composable.invoke(data)
    }

    LaunchedEffect(isColorProceededWith) {
        val targetValue = calcAnimDestDive()
        if (isColorProceededWith) {
            diveAnimatable.animateTo(
                targetValue = targetValue,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 50f,
                ),
            )
        } else {
            diveAnimatable.animateTo(
                targetValue = targetValue,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 100f,
                ),
            )
            mutablePacedDataFlow.value = actualDataFlow.value
        }
    }
}

@Stable
private fun ColorPreviewAnimState.calcAnimationDive(
    containerSize: DpSize?,
    previewSize: DpSize?,
    previewPositionInContainer: DpOffset?,
): Dp {
    when (this) {
        ColorPreviewAnimState.Initial ->
            return 0.dp
        ColorPreviewAnimState.Dived -> {
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
            isColorProceededWith = false,
            containerSize = DpSize(width = 150.dp, height = 400.dp),
            containerPositionInRoot = DpOffset.Zero,
        )
    }
}