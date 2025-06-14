package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.ui.unit.dp
import arrow.optics.copy
import arrow.optics.optics
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * State of UI animation of 'Home' View.
 * It can be used to either describe a current state of the UI in terms of animation sequence,
 * or to define a destination animation state that UI should animate to.
 */
@optics
/* internal but @optics */
data class HomeAnimState(
    val colorPreview: ColorPreview,
    val colorCenter: ColorCenter,
) {

    @optics
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

        // The @optics lenses are generated in the companion object
        companion object
    }

    enum class ColorCenter {
        Collapsed, Expanded;
    }

    // The @optics lenses are generated in the companion object
    companion object
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
 * Infers appropriate (initial or target) [HomeAnimState] based on the specified state of 'Home' feature.
 */
@Suppress("KotlinConstantConditions")
internal fun HomeAnimState(
    isColorPreviewVisible: Boolean, // is there a valid color in 'Color Input'
    isColorCenterVisible: Boolean, // whether the color was proceeded with
): HomeAnimState {
    if (!isColorPreviewVisible) {
        assert(isColorCenterVisible == false) // transitive assumption according to impl of ViewModels
        return FullForwardSequence[0]
    }
    assert(isColorPreviewVisible == true)
    return when (isColorCenterVisible) {
        false -> FullForwardSequence[1]
        true -> FullForwardSequence.last()
    }
}

internal class HomeAnimSequence(
    private val states: List<HomeAnimState>,
) : List<HomeAnimState> by states {

    init {
        require(states.size >= 2) { "Sequence must have at least 2 states: start and finish" }
    }

    // inheritance 'by' delegate doesn't inherit methods of 'Any'
    override fun toString(): String =
        states.toString()
}

private val FullForwardSequence: HomeAnimSequence = run {
    val states = buildList {
        // 0
        HomeAnimStates.Collapsed
            .also { add(it) }
        // 1
        last().copy {
            HomeAnimState.colorPreview.visibility set ColorPreview.Visibility.Visible
        }.also { add(it) }
        // 2
        last().copy {
            HomeAnimState.colorPreview.position set ColorPreview.Position.Dived
        }.also { add(it) }
        // 3
        last().copy {
            HomeAnimState.colorCenter set ColorCenter.Expanded
        }.also { add(it) }
    }
    assert(states.last() == HomeAnimStates.Expanded)
    HomeAnimSequence(states)
}

internal fun HomeAnimSequence(
    from: HomeAnimState,
    to: HomeAnimState,
): HomeAnimSequence {
    require(from in FullForwardSequence)
    require(to in FullForwardSequence)
    val indexOfCurrent = FullForwardSequence.indexOf(from) // -1 is impossible due to 'contains()' check above
    val indexOfDest = FullForwardSequence.indexOf(to) // -1 is impossible due to 'contains()' check above
    val subsequence = when {
        indexOfCurrent < indexOfDest ->
            FullForwardSequence.subList(indexOfCurrent, indexOfDest + 1)
        indexOfCurrent > indexOfDest ->
            FullForwardSequence.subList(indexOfDest, indexOfCurrent + 1).reversed()
        else -> {
            assert(indexOfCurrent == indexOfDest)
            val state = FullForwardSequence[indexOfCurrent]
            listOf(state, state)
        }
    }
    require(subsequence.isNotEmpty()) { "Cannot make 'HomeAnimSequence' from $from to $to" }
    return HomeAnimSequence(subsequence)
}

internal class HomeAnimController(
    currentState: HomeAnimState,
) {
    var currentState: HomeAnimState = currentState
    val flowOfDestState = MutableStateFlow(currentState)

    private var runningSequenceIterator: Iterator<HomeAnimState>? = null
    private var isRunning = false

    fun run(sequence: HomeAnimSequence) {
        require(sequence.first() == currentState) { "sequence must start from current state" }
        val sequenceIterator = sequence.drop(1).listIterator() // drop first 'currentState'
        val oldDest = flowOfDestState.value
        val newDest = requireNotNull(nextInSequence(sequenceIterator))
        if (oldDest != newDest) {
            flowOfDestState.value = newDest
            runningSequenceIterator = sequenceIterator
            isRunning = true
        }
    }

    fun reportDestReached(dest: ColorPreview.Position) {
        if (!isRunning) return
        currentState = currentState.copy {
            HomeAnimState.colorPreview.position set dest
        }
        checkIfDestIsReachedAndSetNext()
    }

    fun reportDestReached(dest: ColorPreview.Visibility) {
        if (!isRunning) return
        currentState = currentState.copy {
            HomeAnimState.colorPreview.visibility set dest
        }
        checkIfDestIsReachedAndSetNext()
    }

    fun reportDestReached(dest: ColorCenter) {
        if (!isRunning) return
        currentState = currentState.copy {
            HomeAnimState.colorCenter set dest
        }
        checkIfDestIsReachedAndSetNext()
    }

    private fun nextInSequence(): HomeAnimState? {
        val sequenceIterator = requireNotNull(runningSequenceIterator)
        return nextInSequence(iterator = sequenceIterator)
    }

    private fun nextInSequence(iterator: Iterator<HomeAnimState>): HomeAnimState? =
        if (iterator.hasNext()) iterator.next() else null

    private fun checkIfDestIsReachedAndSetNext() {
        val currentDest = flowOfDestState.value
        if (currentState != currentDest) return

        if (!isRunning) return // if running, update next dest
        assert(currentState == currentDest) // dest is reached
        val nextDest = nextInSequence()
        if (nextDest != null) {
            flowOfDestState.value = nextDest
        } else {
            runningSequenceIterator = null
            isRunning = false
        }
    }
}

/**
 * 'Color Center's focal point is a point on the screen that is the center of the clipping circle
 * in its circular reveal animation.
 * It is also a point where 'Color Preview' dives to and rises from during its animation.
 */
internal val ColorCenterFocalPointBottomOffset = 96.dp