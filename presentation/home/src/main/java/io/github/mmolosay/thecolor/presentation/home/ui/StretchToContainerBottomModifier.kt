package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import kotlin.math.roundToInt

private typealias ContainerViewportHeight = () -> Int?
private typealias ContainerPosInRoot = () -> Offset?

/**
 * Stretches the element vertically, so that its bottom edge matches the bottom edge of its container.
 *
 * The getters are invoked during the layout phase. Reads of a snapshot state performed inside them
 * are observed: changing such state invalidates measurement of this element, skipping recomposition
 * entirely. Reading a value that is not a snapshot state will *not* update the element.
 *
 * Lambda getters must be stable across recompositions, i.e. the very same instances must be passed.
 * Otherwise, measurement of this element is invalidated on every recomposition of the caller.
 * See how [StretchToContainerBottomUtils] [remember]s returned lambdas.
 * See [androidx.compose.foundation.layout.offset] for the example of similar approach.
 *
 * @param containerViewportHeight height of the visible part of the container, in pixels.
 * `null` means "not measured yet", in which case this modifier is no-op.
 *
 * @param containerPosInRoot position of the container's content in the root layout.
 * `null` means "not placed yet", in which case this modifier is no-op.
 */
fun Modifier.stretchToContainerBottom(
    containerViewportHeight: ContainerViewportHeight,
    containerPosInRoot: ContainerPosInRoot,
): Modifier =
    this then StretchToContainerBottomElement(
        containerViewportHeight = containerViewportHeight,
        containerPosInRoot = containerPosInRoot,
    )

object StretchToContainerBottomUtils {

    @Composable
    fun rememberContainerViewportHeight(
        containerScrollState: ScrollState,
    ): ContainerViewportHeight = remember(containerScrollState) {
        { containerScrollState.viewportSize.takeUnless { it == 0 } }
    }

    @Composable
    fun rememberContainerPosInRoot(
        stateOfContainerPosInRoot: State<Offset?>,
    ): ContainerPosInRoot = remember(stateOfContainerPosInRoot) {
        { stateOfContainerPosInRoot.value }
    }
}

private data class StretchToContainerBottomElement(
    private val containerViewportHeight: ContainerViewportHeight,
    private val containerPosInRoot: ContainerPosInRoot,
) : ModifierNodeElement<StretchToContainerBottomNode>() {

    override fun create() =
        StretchToContainerBottomNode(
            containerViewportHeight = containerViewportHeight,
            containerPosInRoot = containerPosInRoot,
        )

    override fun update(node: StretchToContainerBottomNode) {
        node.containerViewportHeight = containerViewportHeight
        node.containerPosInRoot = containerPosInRoot
    }

    @Suppress("SpellCheckingInspection")
    override fun InspectorInfo.inspectableProperties() {
        name = "stretchToContainerBottom"
        properties["containerViewportHeight"] = containerViewportHeight
        properties["containerPosInRoot"] = containerPosInRoot
    }
}

private class StretchToContainerBottomNode(
    var containerViewportHeight: ContainerViewportHeight,
    var containerPosInRoot: ContainerPosInRoot,
) : Modifier.Node(),
    LayoutModifierNode,
    GlobalPositionAwareModifierNode {

    private var ownYPosInContainer: Float? = null

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val containerPosInRoot = containerPosInRoot() ?: return
        val ownPosInRoot = coordinates.positionInRoot()
        val newOwnYPosInContainer = (ownPosInRoot - containerPosInRoot).y
        if (newOwnYPosInContainer == ownYPosInContainer) return // scrolling moves both equally
        ownYPosInContainer = newOwnYPosInContainer
        invalidateMeasurement()
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val minHeight = calcMinHeight() ?: 0
        val newConstraints = constraints.copy(
            minHeight = constraints.constrainHeight(minHeight),
        )
        val placeable = measurable.measure(newConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(x = 0, y = 0)
        }
    }

    private fun calcMinHeight(): Int? {
        val containerViewportHeight = containerViewportHeight() ?: return null // observed
        val ownYPosInContainer = ownYPosInContainer ?: return null
        val minHeight = containerViewportHeight - ownYPosInContainer
        return minHeight.roundToInt().coerceAtLeast(0)
    }
}