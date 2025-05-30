package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * State of UI animation of 'Home' View.
 * It can be used to either describe a current state of the UI in terms of animation sequence,
 * or to define a destination animation state that UI should animate to.
 */
internal data class HomeAnimState(
    val colorPreview: ColorPreview,
    val colorCenter: ColorCenter,
) {

    data class ColorPreview(
        val position: Position,
        val visibility: Visibility,
    ) {
        enum class Position {
            NotDived, Dived;
        }
        enum class Visibility {
            Visible, Hidden;
        }
    }

    enum class ColorCenter {
        Collapsed, Expanded;
    }

    // TODO: try Arrow.io lenses
    fun copy(
        colorPreviewPosition: ColorPreview.Position = this.colorPreview.position,
        colorPreviewVisibility: ColorPreview.Visibility = this.colorPreview.visibility,
        colorCenter: ColorCenter = this.colorCenter,
    ) =
        copy(
            colorPreview = colorPreview.copy(
                position = colorPreviewPosition,
                visibility = colorPreviewVisibility,
            ),
            colorCenter = colorCenter,
        )
}

private object HomeAnimStates {

    val Collapsed = HomeAnimState(
        colorPreview = ColorPreview(
            position = ColorPreview.Position.NotDived,
            visibility = ColorPreview.Visibility.Hidden,
        ),
        colorCenter = ColorCenter.Collapsed,
    )

    val Expanded = HomeAnimState(
        colorPreview = ColorPreview(
            position = ColorPreview.Position.Dived,
            visibility = ColorPreview.Visibility.Visible,
        ),
        colorCenter = ColorCenter.Expanded,
    )
}

/**
 * Infers which [HomeAnimState] to use based on the current state of 'Home' feature.
 */
internal fun HomeAnimState(
    isColorProceededWith: Boolean,
): HomeAnimState =
    when (isColorProceededWith) {
        false -> HomeAnimStates.Collapsed
        true -> HomeAnimStates.Expanded
    }

// TODO: abolish type? Replace with typealias?
internal class HomeAnimSequence(states: List<HomeAnimState>) : List<HomeAnimState> by states {

    // TODO: rework? extract into List utils?
    fun nextAfter(state: HomeAnimState): HomeAnimState? {
        val indexOfState = indexOf(state)
        if (indexOfState == -1) return null
        return getOrNull(indexOfState + 1)
    }
}

/**
 * Infers which [HomeAnimSequence] to use based on the updated state of 'Home' feature.
 */
internal fun HomeAnimSequence(
    isColorProceededWith: Boolean,
): HomeAnimSequence =
    when (isColorProceededWith) {
        true -> HomeAnimSequences.ForwardFull
        false -> HomeAnimSequences.BackwardFull
    }

private object HomeAnimSequences {

    val ForwardFull = kotlin.run {
        // TODO: use list builder with last().copy(..)
        val state0 = HomeAnimStates.Collapsed
        val state1 = state0.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        val state2 = state1.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        val state3 = state2.copy(colorCenter = ColorCenter.Expanded)
        assert(state3 == HomeAnimStates.Expanded)
        HomeAnimSequence(listOf(state0, state1, state2, state3))
    }

    val BackwardFull = HomeAnimSequence(ForwardFull.reversed())
}

internal class HomeAnimController(
    initialState: HomeAnimState,
) {

    private var currentState: HomeAnimState = initialState
    val destState = MutableStateFlow(initialState)
    private var sequence: HomeAnimSequence? = null

    fun updateSequence(sequence: HomeAnimSequence) {
        this.sequence = sequence
    }

    fun start() {
        val sequence = requireNotNull(sequence)
        require(currentState in sequence)
        val dest = sequence.nextAfter(currentState) ?: currentState // sequence is already finished
        destState.value = requireNotNull(dest)
    }

    fun reportDestReached(dest: ColorPreview.Position) {
        currentState = currentState.copy(colorPreviewPosition = dest)
        updateDestStateWithNextInSequence()
    }

    fun reportDestReached(dest: ColorPreview.Visibility) {
        currentState = currentState.copy(colorPreviewVisibility = dest)
        updateDestStateWithNextInSequence()
    }

    fun reportDestReached(dest: ColorCenter) {
        currentState = currentState.copy(colorCenter = dest)
        updateDestStateWithNextInSequence()
    }

    private fun updateDestStateWithNextInSequence() {
        val sequence = sequence ?: return
        val indexOfCurrentState = sequence.indexOf(currentState)
        val iterator = sequence.listIterator(indexOfCurrentState + 1)
        if (!iterator.hasNext()) return // sequence is finished
        destState.value = iterator.next()
    }
}

/**
 * 'Color Center's focal point is a point on the screen that is the center of the clipping circle
 * in its circular reveal animation.
 * It is also a point where 'Color Preview' dives to and rises from during its animation.
 */
internal val ColorCenterFocalPointBottomOffset = 96.dp