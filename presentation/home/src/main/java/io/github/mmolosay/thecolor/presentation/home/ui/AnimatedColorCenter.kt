package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.github.mmolosay.thecolor.presentation.impl.CircularReveal
import io.github.mmolosay.thecolor.presentation.impl.RadiusProvider
import io.github.mmolosay.thecolor.presentation.impl.calcVisibleHeightInScrollableContainer
import io.github.mmolosay.thecolor.presentation.impl.clipCircle
import io.github.mmolosay.thecolor.presentation.impl.retainedNotNull
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Animates 'Color Center's circular reveal.
 */
@Composable
internal fun AnimatedColorCenter(
    colorCenter: ColorCenterComposable?,
    flowOfAnimDest: StateFlow<HomeAnimState.ColorCenter>,
    onReached: (reached: HomeAnimState.ColorCenter) -> Unit,
    containerScrollState: ScrollState,
) {
    val density = LocalDensity.current
    val retainedColorCenter by retainedNotNull(colorCenter)

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
            onReached(animDest)
        }
    }

    CircularReveal(
        animProgress = progressAnimatable.value,
    ) {
        var visibleHeightInParent by remember { mutableStateOf<Float?>(null) }
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    val ownPosInParent = coordinates.positionInParent()
                    visibleHeightInParent = calcVisibleHeightInScrollableContainer(
                        containerScrollState = containerScrollState,
                        ownPosInContainer = ownPosInParent.y,
                    )
                }
                .clipCircle(
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
                ),
        ) {
            retainedColorCenter?.invoke()
        }
    }
}