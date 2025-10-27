package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import io.github.mmolosay.thecolor.presentation.common.compose.CircularReveal
import io.github.mmolosay.thecolor.presentation.common.compose.RadiusProvider
import io.github.mmolosay.thecolor.presentation.common.compose.calcVisibleHeightInScrollableContainer
import io.github.mmolosay.thecolor.presentation.common.compose.clipCircle
import io.github.mmolosay.thecolor.presentation.common.compose.retainedNotNull
import io.github.mmolosay.thecolor.presentation.common.compose.thenIf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Animates 'Color Center's circular reveal.
 */
@Composable
internal fun AnimatedColorCenter(
    colorCenter: (@Composable () -> Unit)?,
    flowOfAnimDest: StateFlow<HomeAnimState.ColorCenter>,
    onReached: (reached: HomeAnimState.ColorCenter) -> Unit,
    containerScrollState: ScrollState,
) {
    val density = LocalDensity.current
    val stateOfRetainedColorCenter = retainedNotNull(colorCenter)

    fun HomeAnimState.ColorCenter.targetValue() =
        when (this) {
            HomeAnimState.ColorCenter.Expanded -> 1f
            HomeAnimState.ColorCenter.Collapsed -> 0f
        }

    val progressAnimatable = remember {
        val initialValue = flowOfAnimDest.value.targetValue()
        Animatable(initialValue)
    }
    LaunchedEffect(Unit) {
        flowOfAnimDest.collectLatest collect@{ animDest ->
            val targetValue = animDest.targetValue()
            if (progressAnimatable.value == targetValue) {
                onReached(animDest)
                return@collect // already in target state
            }
            val animSpec: AnimationSpec<Float> = when (animDest) {
                HomeAnimState.ColorCenter.Expanded -> spring(stiffness = 100f)
                HomeAnimState.ColorCenter.Collapsed -> spring(stiffness = 300f)
            }
            progressAnimatable.animateTo(
                targetValue = targetValue,
                animationSpec = animSpec,
            )
            if (progressAnimatable.value == 0f) {
                stateOfRetainedColorCenter.value = null // free for GC
            }
            onReached(animDest)
        }
    }

    CircularReveal(
        stateOfAnimProgress = progressAnimatable.asState(),
    ) {
        var visibleHeightInParent by remember { mutableStateOf<Float?>(null) }
        val shouldAddClipCircleModifier by remember {
            derivedStateOf { progressAnimatable.isRunning }
        }
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    val ownPosInParent = coordinates.positionInParent()
                    visibleHeightInParent = calcVisibleHeightInScrollableContainer(
                        containerScrollState = containerScrollState,
                        ownPosInContainer = ownPosInParent.y,
                    )
                }
                /*
                 * 1. Scrolling outer container updates 'visibleHeightInParent'.
                 * 2. 'drawWithCache()' detects this because it is being read in 'center()' lambda and re-draws 'clipCircle()'.
                 * To avoid unnecessary re-draws, don't apply 'clipCircle()' if it won't make the difference in UI.
                 */
                .thenIf(shouldAddClipCircleModifier) {
                    clipCircle(
                        center = { size ->
                            val h = visibleHeightInParent
                            if (h != null && h != 0f) {
                                val focalPointOffset =
                                    with(density) { ColorCenterFocalPointBottomOffset.toPx() }
                                val y = (h - focalPointOffset).coerceAtLeast(0f)
                                Offset(x = size.width / 2, y = y)
                            } else {
                                size.center
                            }
                        },
                        radius = RadiusProvider { size, minCoverRadius ->
                            minCoverRadius * progressAnimatable.value
                        },
                    )
                },
        ) {
            val colorCenter = stateOfRetainedColorCenter.value
            colorCenter?.invoke()
        }
    }
}