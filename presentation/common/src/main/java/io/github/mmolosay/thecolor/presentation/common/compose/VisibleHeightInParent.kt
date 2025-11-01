package io.github.mmolosay.thecolor.presentation.common.compose

import androidx.compose.foundation.ScrollState

/**
 * Calculates height (vertical amount) of the element that is visible in container (parent or ancestor).
 *
 * @param containerViewportHeight display height of the container.
 * Size as-is for non-scrollable containers. Viewport height for scrollable ones.
 * @param containerScrolledAmount vertically scrolled amount of the container (if scrollable, otherwise use 0).
 * @param ownPosInContainer vertical position of the element inside the container. Implies "true" position, not
 * a position in container's viewport.
 *
 * @return full height of the element if it's not clipped by the container, otherwise visible height.
 */
fun calcVisibleHeightInContainer(
    containerViewportHeight: Float,
    containerScrolledAmount: Float,
    ownPosInContainer: Float,
): Float {
    val containerVisibleBottom = containerViewportHeight + containerScrolledAmount
    val visibleHeightInContainer = containerVisibleBottom - ownPosInContainer
    return visibleHeightInContainer
}

fun calcVisibleHeightInScrollableContainer(
    containerScrollState: ScrollState,
    ownPosInContainer: Float,
): Float =
    calcVisibleHeightInContainer(
        containerViewportHeight = containerScrollState.viewportSize.toFloat(),
        containerScrolledAmount = containerScrollState.value.toFloat(),
        ownPosInContainer = ownPosInContainer,
    )