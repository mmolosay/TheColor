package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.ui.unit.dp
import arrow.optics.copy
import arrow.optics.optics
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

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
    return HomeAnimSequence(subsequence)
}

internal class HomeAnimController(
    currentState: HomeAnimState,
) {
    var currentState: HomeAnimState = currentState
        private set

    val flowOfDestState = MutableStateFlow(currentState)

    private val destState: HomeAnimState
        get() = flowOfDestState.value

    private var runningSequence: IterableSequence? = null

    val isRunning: Boolean
        get() = (runningSequence != null)

    fun run(sequence: HomeAnimSequence) {
        require(sequence.first() == currentState) { "sequence must start from current state" }
        kotlin.run doNotRunIfSequenceIsNoOp@{
            val destStates = sequence.drop(1) // current state
            if (destStates.all { it == destState }) return // sequence is no-op
        }
        val runningSequence = IterableSequence(sequence = sequence, index = 0)
        flowOfDestState.value = requireNotNull(runningSequence.advance())
        this.runningSequence = runningSequence
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
        Timber.d("DBG | updated currentState = $currentState")
        checkIfDestIsReachedAndSetNext()
    }

    private fun checkIfDestIsReachedAndSetNext() {
        if (currentState != destState) return // dest is not reached yet

        if (!isRunning) return // if running, update next dest
        assert(currentState == destState) // dest is reached
        val nextState = requireNotNull(runningSequence).advance()
        if (nextState != null) {
            flowOfDestState.value = nextState
            Timber.d("DBG | updated destState = $nextState") // TODO: remove me
        } else {
            runningSequence = null // sequence is finished
            Timber.d("DBG | sequence is finished") // TODO: remove me
        }
    }

    // TODO: return back to simple iterator if 'sequence' property remains unused
    private data class IterableSequence(
        val sequence: HomeAnimSequence,
        var index: Int,
    ) {
        val currentState: HomeAnimState?
            get() = sequence.getOrNull(index)

        fun advance(): HomeAnimState? {
            index += 1
            return currentState
        }
    }
}

/**
 * 'Color Center's focal point is a point on the screen that is the center of the clipping circle
 * in its circular reveal animation.
 * It is also a point where 'Color Preview' dives to and rises from during its animation.
 */
internal val ColorCenterFocalPointBottomOffset = 96.dp