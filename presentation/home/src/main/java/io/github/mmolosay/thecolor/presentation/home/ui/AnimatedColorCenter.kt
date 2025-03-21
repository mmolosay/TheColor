package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.impl.CircularReveal
import io.github.mmolosay.thecolor.presentation.impl.CircularRevealAnimator
import io.github.mmolosay.thecolor.presentation.impl.RadiusProvider
import io.github.mmolosay.thecolor.presentation.impl.calcVisibleHeightInScrollableContainer
import io.github.mmolosay.thecolor.presentation.impl.clipCircle
import io.github.mmolosay.thecolor.presentation.impl.retainedNotNull
import io.github.mmolosay.thecolor.utils.cache.CacheStore
import io.github.mmolosay.thecolor.utils.cache.DequeCache
import io.github.mmolosay.thecolor.utils.cache.PruneOnSizeThreshold
import kotlinx.coroutines.launch

@Composable
internal fun AnimatedColorCenter(
    colorCenter: ColorCenterComposable?,
    animDest: HomeAnimState.ColorCenter,
    onAnimDestReached: (HomeAnimState.ColorCenter) -> Unit,
    containerScrollState: ScrollState,
) {
    val coroutineScope = rememberCoroutineScope()

    val circularRevealAnimator = remember {
        val initialProgressValue = when (animDest) {
            HomeAnimState.ColorCenter.Expanded -> CircularRevealAnimator.FullyExpandedValue
            HomeAnimState.ColorCenter.Collapsed -> CircularRevealAnimator.FullyCollapsedValue
        }
        CircularRevealAnimator(
            progressAnimatable = Animatable(initialValue = initialProgressValue),
            expandAnimationSpec = spring(stiffness = 100f),
            collapseAnimationSpec = spring(stiffness = 300f),
        )
    }
    LaunchedEffect(animDest) {
        coroutineScope.launch {
            when (animDest) {
                HomeAnimState.ColorCenter.Expanded -> circularRevealAnimator.expand()
                HomeAnimState.ColorCenter.Collapsed -> circularRevealAnimator.collapse()
            }
            onAnimDestReached(animDest)
        }
    }

    CircularReveal(
        animator = circularRevealAnimator,
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
                        if (h != null && h != 0f) Offset(x = size.width / 2, y = h)
                        else size.center
                    },
                    radius = RadiusProvider { size, minCoverRadius ->
                        minCoverRadius * circularRevealAnimator.progressAnimatable.value
                    },
                ),
        ) {
            colorCenter?.invoke()
        }
    }
}

/**
 * Contains "container" in name to convey that this Composable may or
 * may not display [colorCenter], which is its primary content.
 */
// TODO: extract into AnimatedColorCenter file?
@Composable
private fun ColorCenterContainer(
    colorCenter: ColorCenterComposable?,
    proceedResult: ProceedResult?,
    cacheStore: CacheStore,
    navBarAppearanceController: NavBarAppearanceController,
    parentScrollState: ScrollState,
) {
    val coroutineScope = rememberCoroutineScope()

    val proceedResultCacheTag = CacheStore.Tag("ProceedResultCacheTag")
    val proceedResultCache = cacheStore.getOrNew<ProceedResult?>(proceedResultCacheTag) {
        DequeCache(
            mutationListener = PruneOnSizeThreshold(
                cacheSizeThreshold = 10, numberOfLatestElementsToKeep = 2,
            ),
        )
    }

    fun currentAndPreviousFromCache(): Pair<ProceedResult?, ProceedResult?> {
        val values = proceedResultCache.asReversed()
        val current = values.getOrNull(0)
        val previous = values.getOrNull(1)
        return Pair(current, previous)
    }

    fun hasProceedResultBecomeSuccess(): Boolean {
        val (current, previous) = currentAndPreviousFromCache()
        return (current is ProceedResult.Success && previous !is ProceedResult.Success)
    }

    fun hasProceedResultBecomeNull(): Boolean {
        val (current, previous) = currentAndPreviousFromCache()
        return (current == null && previous is ProceedResult.Success)
    }

//    val retainedProceedResult = retained(proceedResult) { actual, memoized ->
//        val memoizedIsSuccess = (memoized is ProceedResult.Success)
//        val actualIsNotSuccess = (actual !is ProceedResult.Success)
//        if (memoizedIsSuccess && actualIsNotSuccess) {
//            delay(2.framesDuration)
//        }
//        value = actual
//    }
    val retainedProceedResult = proceedResult
    LaunchedEffect(retainedProceedResult) {
        proceedResultCache += retainedProceedResult
    }

    val circularRevealAnimator = remember {
        val initialProgressValue = when {
            retainedProceedResult is ProceedResult.Success -> CircularRevealAnimator.FullyExpandedValue
            else -> CircularRevealAnimator.FullyCollapsedValue
        }
        CircularRevealAnimator(
            progressAnimatable = Animatable(initialValue = initialProgressValue),
            expandAnimationSpec = spring(stiffness = 100f),
            collapseAnimationSpec = spring(stiffness = 300f),
        )
    }
    LaunchedEffect(retainedProceedResult) {
        when {
            hasProceedResultBecomeSuccess() -> {
                coroutineScope.launch { circularRevealAnimator.expand() }
            }
            hasProceedResultBecomeNull() -> {
                coroutineScope.launch { circularRevealAnimator.collapse() }
            }
        }
    }

    /** Wrapper for decorated [colorCenter] with specific positioning inside the parent and animations. */
    @Composable
    fun ColorCenter(
        data: ProceedResult.Success,
        colorCenter: ColorCenterComposable,
    ) {
        var visibleHeightInParent by remember { mutableStateOf<Float?>(null) }
//        DecoratedColorCenter(
//            modifier = Modifier
//                .onGloballyPositioned { coordinates ->
//                    val ownPosInParent = coordinates.positionInParent()
//                    visibleHeightInParent = calcVisibleHeightInScrollableParent(
//                        parentScrollState = parentScrollState,
//                        ownPosInParent = ownPosInParent.y,
//                    )
//                }
//                .clipCircle(
//                    center = { size ->
//                        val h = visibleHeightInParent
//                        if (h != null && h != 0f) Offset(x = size.width / 2, y = h)
//                        else size.center
//                    },
//                    radius = RadiusProvider { size, minCoverRadius ->
//                        minCoverRadius * circularRevealAnimator.progressAnimatable.value
//                    },
//                ),
//            surfaceColor = data.colorData.color.toCompose(),
//            isSurfaceColorDark = data.colorData.isDark,
//            colorCenter = colorCenter,
//            navBarAppearanceController = navBarAppearanceController,
//            minHeight = minHeight,
//        )
    }

    val actualColorCenter: ColorCenterComposable? =
        remember(colorCenter, retainedProceedResult) {
            colorCenter ?: return@remember null
            val retainedAsSuccess =
                (retainedProceedResult as? ProceedResult.Success) ?: return@remember null
            ColorCenterComposable {
                ColorCenter(
                    data = retainedAsSuccess,
                    colorCenter = colorCenter,
                )
            }
        }
    val retainedColorCenter = retainedNotNull(actualValue = actualColorCenter)

    CircularReveal(
        animator = circularRevealAnimator,
    ) {
        retainedColorCenter?.invoke()
    }
}