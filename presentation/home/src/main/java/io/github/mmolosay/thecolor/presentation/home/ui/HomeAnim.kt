package io.github.mmolosay.thecolor.presentation.home.ui

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

    enum class ColorPreview {
        Initial, Dived;
    }

    enum class ColorCenter {
        Collapsed, Expanded;
    }
}

/**
 * Produces initial [HomeAnimState] to be used when 'Home' View has just been displayed.
 */
internal fun initialHomeAnimState(
    isColorProceededWith: Boolean,
) =
    when (isColorProceededWith) {
        false -> HomeAnimSequences.ForwardFull.first()
        true -> HomeAnimSequences.BackwardFull.first()
    }

internal class HomeAnimSequence(states: List<HomeAnimState>) : List<HomeAnimState> by states {

    // TODO: rework? extract into List utils?
    fun nextAfter(state: HomeAnimState): HomeAnimState? {
        val indexOfState = indexOf(state)
        if (indexOfState == -1) return null
        return getOrNull(indexOfState + 1)
    }
}

internal object HomeAnimSequences {

    val ForwardFull = kotlin.run {
        val state0 = HomeAnimState(
            colorPreview = ColorPreview.Initial,
            colorCenter = ColorCenter.Collapsed,
        )
        val state1 = state0.copy(colorPreview = ColorPreview.Dived)
        val state2 = state1.copy(colorCenter = ColorCenter.Expanded)
        HomeAnimSequence(listOf(state0, state1, state2))
    }

    val BackwardFull = HomeAnimSequence(ForwardFull.reversed())
}

internal class HomeAnimController(
    currentState: HomeAnimState,
) {

    private var currentState: HomeAnimState = currentState
    val destState = MutableStateFlow(currentState)
    private var sequence: HomeAnimSequence? = null

    fun updateSequence(sequence: HomeAnimSequence) {
        this.sequence = sequence
    }

    fun start() {
        val dest = requireNotNull(sequence).nextAfter(currentState)
        destState.value = requireNotNull(dest)
    }

    fun reportDestReached(dest: ColorPreview) {
        currentState = currentState.copy(colorPreview = dest)
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