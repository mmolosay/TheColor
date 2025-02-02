package io.github.mmolosay.thecolor.presentation.impl

import androidx.compose.foundation.ScrollState

/**
 * Calculates height (vertical amount) of the element that is visible in its parent.
 *
 * @param parentViewportHeight display height of the parent.
 * Size as-is for non-scrollable containers. Viewport height for scrollable ones.
 * @param parentScrolledAmount vertically scrolled amount of the parent (if scrollable, otherwise use 0).
 * @param ownPosInParent vertical position of the element inside the parent. Implies "true" position, not
 * a position in parent's viewport.
 *
 * @return full height of the element if it's not clipped by the parent, otherwise visible height.
 */
fun calcVisibleHeightInParent(
    parentViewportHeight: Float,
    parentScrolledAmount: Float,
    ownPosInParent: Float,
): Float {
    val parentVisibleBottom = parentViewportHeight + parentScrolledAmount
    val visibleHeightInParent = parentVisibleBottom - ownPosInParent
    return visibleHeightInParent
}

fun calcVisibleHeightInScrollableParent(
    parentScrollState: ScrollState,
    ownPosInParent: Float,
): Float =
    calcVisibleHeightInParent(
        parentViewportHeight = parentScrollState.viewportSize.toFloat(),
        parentScrolledAmount = parentScrollState.value.toFloat(),
        ownPosInParent = ownPosInParent,
    )