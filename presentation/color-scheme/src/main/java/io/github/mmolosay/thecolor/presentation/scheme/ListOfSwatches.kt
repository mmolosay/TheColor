package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.mmolosay.thecolor.presentation.common.compose.drawIf
import io.github.mmolosay.thecolor.presentation.common.compose.rememberSnapshotFlow
import io.github.mmolosay.thecolor.utils.DebugOnly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ListOfSwatches(
    swatches: List<ColorSchemeData.Swatch>,
    onSwatchClick: (indexOfSwatch: Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { swatches.size },
    )

    val pagerScrollMutex = remember { Mutex() } // multiple calls to 'scrollToPage()' simultaneously doesn't work as fair FIFO by default
    suspend fun PagerState.scrollToPageFairly(page: Int) =
        pagerScrollMutex.withLock {
            this.scrollToPage(page)
        }

    val contentPadding by calcContentPadding(pagerState)
    val isReadyToBeDrawn = (contentPadding != null)
    val flowOfIsReadyToBeDrawn = rememberSnapshotFlow(isReadyToBeDrawn)

    LaunchedEffect(swatches) {
        pagerState.scrollToPageFairly(page = 0)
    }
    // 'rememberPagerState()' employs 'rememberSaveable()', which allows to recall last value on "first" composition
    val recalledLastSettledPage = remember { pagerState.settledPage }
    LaunchedEffect(Unit) {
        flowOfIsReadyToBeDrawn.first { isReadyToBeDrawn -> isReadyToBeDrawn } // skip until first 'isReadyToBeDrawn' == 'true'
        pagerState.scrollToPageFairly(page = recalledLastSettledPage)
    }
    HorizontalPager(
        modifier = Modifier
            .edgeToEdge(parentTotalHorizontalPadding = 32.dp)
            .drawIf(isReadyToBeDrawn)
            .fillMaxWidth(),
        state = pagerState,
        contentPadding = contentPadding ?: PaddingValues(0.dp),
        pageSize = PageSize.Fixed(pageSize = SwatchSize),
        pageSpacing = SwatchSpacing,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(pages = swatches.size),
        ),
        userScrollEnabled = isReadyToBeDrawn,
        snapPosition = SnapPosition.Center,
    ) { pageIndex ->
        val swatch = swatches[pageIndex]
        val zIndex by remember {
            derivedStateOf {
                val currentPage = pagerState.currentPage
                val maxPageIndex = pagerState.pageCount - 1
                fun indexFraction() = (pageIndex.toFloat() / maxPageIndex)
                when {
                    pageIndex == currentPage -> 2f // biggest z-index to draw current page above any other
                    // if calculated z-index doesn't depend on current page, then only two swatches will recompose: old current page and new one
                    pageIndex < currentPage ->
                        indexFraction() // increasing z-index before current page
                    pageIndex > currentPage ->
                        1f - indexFraction() // decreasing z-index after current page
                    else -> error("unreachable")
                }
            }
        }
        val scale by remember {
            derivedStateOf {
                val currentPageWithOffset =
                    pagerState.currentPage + pagerState.currentPageOffsetFraction
                val delta = (pageIndex - currentPageWithOffset).absoluteValue
                val fraction = 1f - delta.coerceIn(0f, 1f)
                SwatchMinScale + (SwatchMaxScale - SwatchMinScale) * fraction
            }
        }
        Swatch(
            modifier = Modifier
                .zIndex(zIndex)
                .scale(scale),
            swatch = swatch,
            onClick = { onSwatchClick(pageIndex) },
        )
    }
}

internal const val SwatchMinScale = 0.8f
internal const val SwatchMaxScale = 1f
internal val SwatchSpacing = run {
    val spaceBetweenAdjacentMinScaledSwatches = SwatchSize * (SwatchMaxScale - SwatchMinScale)
    val halfOfMinScaledSwatchOffset = (SwatchSize * SwatchMinScale) / 2f
    -(spaceBetweenAdjacentMinScaledSwatches + halfOfMinScaledSwatchOffset)
}

@Composable
private fun calcContentPadding(pagerState: PagerState): State<PaddingValues?> {
    val density = LocalDensity.current
    // PagerState.layoutInfo is a @FrequentlyChangingValue (not annotated but it is, just like in LazyListState)
    return remember {
        derivedStateOf(structuralEqualityPolicy()) {
            val layoutInfo = pagerState.layoutInfo
            val viewportSize = layoutInfo.viewportSize
            val orientation = layoutInfo.orientation
            calcContentPadding(
                density = density,
                viewportSize = viewportSize,
                orientation = orientation,
            )
        }
    }
}

private fun calcContentPadding(
    density: Density,
    viewportSize: IntSize,
    orientation: Orientation,
): PaddingValues? {
    if (viewportSize == IntSize.Zero) return null // undefined value in LayoutInfo implementations
    val itemSize = with(density) { SwatchSize.toPx() }
    val viewportSize = viewportSize.takeIf { it != IntSize.Zero } ?: return null
    val viewportMainDimen = when (orientation) {
        Orientation.Vertical -> viewportSize.height
        Orientation.Horizontal -> viewportSize.width
    }
    val sizeWithoutCenterItem = (viewportMainDimen - itemSize)
    val horizontalPadding = (sizeWithoutCenterItem / 2)
    return PaddingValues(horizontal = with(density) { horizontalPadding.toDp() })
}

private fun Modifier.edgeToEdge(
    parentTotalHorizontalPadding: Dp,
): Modifier =
    layout { measurable, constraints ->
        val expandedConstraints = constraints.copy(
            maxWidth = constraints.maxWidth + parentTotalHorizontalPadding.roundToPx(),
        )
        val placeable = measurable.measure(expandedConstraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(x = 0, y = 0)
        }
    }

/**
 * Draws a circle to make it visually easier to locate a swatch in the center.
 * Applied to the [ListOfSwatches].
 */
@DebugOnly
@Suppress("unused")
private fun Modifier.drawCenterItemPointer(): Modifier =
    this.drawBehind {
        val itemRadius = SwatchSize / 2
        val strokeWidth = 2.dp
        val pointerSpacing = 1.dp
        val pointerRadius = (itemRadius + pointerSpacing + strokeWidth)
        drawCircle(
            color = Color.Red,
            radius = with(density) { pointerRadius.toPx() },
            center = this.size.center,
            style = Stroke(width = with(density) { strokeWidth.toPx() }),
        )
    }