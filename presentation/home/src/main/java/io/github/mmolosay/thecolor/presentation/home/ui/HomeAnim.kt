package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * State of UI animation of 'Home' View.
 * It can be used to either describe a current state of the UI in terms of animation sequence,
 * or to define a destination animation state that UI should animate to.
 */
internal data class HomeAnimState(
    val colorPreviewPosition: ColorPreview.Position,
    val colorPreviewVisibility: ColorPreview.Visibility,
    val colorCenter: ColorCenter,
) {

    object ColorPreview {
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
}

private object HomeAnimStates {

    val Collapsed = HomeAnimState(
        colorPreviewPosition = ColorPreview.Position.NotDived,
        colorPreviewVisibility = ColorPreview.Visibility.Hidden,
        colorCenter = ColorCenter.Collapsed,
    )

    val Expanded = HomeAnimState(
        colorPreviewPosition = ColorPreview.Position.Dived,
        colorPreviewVisibility = ColorPreview.Visibility.Visible,
        colorCenter = ColorCenter.Expanded,
    )
}

/**
 * Infers appropriate (initial or target) [HomeAnimState] based on the specified state of 'Home' feature.
 */
@Suppress("KotlinConstantConditions")
internal fun HomeAnimState(
    isColorPreviewVisible: Boolean,
    isColorCenterVisible: Boolean,
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
        last().copy(
            colorPreviewVisibility = ColorPreview.Visibility.Visible,
        ).also { add(it) }
        // 2
        last().copy(
            colorPreviewPosition = ColorPreview.Position.Dived,
        ).also { add(it) }
        // 3
        last().copy(
            colorCenter = ColorCenter.Expanded,
        ).also { add(it) }
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

// TODO: remove logs
internal class HomeAnimController(
    currentState: HomeAnimState,
) {
    var currentState: HomeAnimState = currentState
        private set

    val flowOfDestState = MutableStateFlow(currentState)

    private val destState: HomeAnimState
        get() = flowOfDestState.value

    private var runningSequence: AdvancingSequence? = null

    val isRunning: Boolean
        get() = (runningSequence != null)

    fun run(sequence: HomeAnimSequence) {
        Timber.d("DBG | ----------------------------")
        Timber.d("DBG | running new sequence $sequence")
        Timber.d("DBG | currentState = $currentState")
        Timber.d("DBG | currentDest = ${flowOfDestState.value}")
        Timber.d("DBG | runningSequence = $runningSequence")
        Timber.d("DBG | ----------------------------")

        require(sequence.first() == currentState) { "sequence must start from current state" }
        runningSequence = AdvancingSequence(sequence)
        setNextDestFromSequence()
    }

    fun reportStateReached(value: ColorPreview.Position) =
        reportStateReached(
            reachedValue = value,
            destValue = destState.colorPreviewPosition,
            applyToCurrentState = { it.copy(colorPreviewPosition = value) },
        )

    fun reportStateReached(value: ColorPreview.Visibility) =
        reportStateReached(
            reachedValue = value,
            destValue = destState.colorPreviewVisibility,
            applyToCurrentState = { it.copy(colorPreviewVisibility = value) },
        )

    fun reportStateReached(value: ColorCenter) =
        reportStateReached(
            reachedValue = value,
            destValue = destState.colorCenter,
            applyToCurrentState = { it.copy(colorCenter = value) },
        )

    fun <T> reportStateReached(
        reachedValue: T,
        destValue: T,
        applyToCurrentState: (HomeAnimState) -> HomeAnimState,
    ) {
        if (!isRunning) return
        Timber.d("DBG | ----------------------------")
        Timber.d("DBG | dest $reachedValue is reached")
        if (reachedValue == destValue) {
            currentState = applyToCurrentState(currentState)
            Timber.d("DBG | updated currentState = $currentState")
            checkIfDestIsReachedAndSetNext()
        } else {
            Timber.d("DBG | $reachedValue is reported as reached but dest is $destValue")
        }
    }

    private fun setNextDestFromSequence() {
        if (!isRunning) return
        val nextState = requireNotNull(runningSequence).advance()
        if (nextState != null) {
            if (nextState != destState || nextState != currentState) {
                flowOfDestState.value = nextState
                Timber.d("DBG | updated destState = $nextState")
            } else {
                setNextDestFromSequence()
            }
        } else {
            runningSequence = null // sequence is finished
            Timber.d("DBG | sequence is finished")
        }
    }

    private fun checkIfDestIsReachedAndSetNext() {
        if (!isRunning) return
        if (currentState == destState) {
            setNextDestFromSequence()
        }
    }

    // TODO: return back to simple iterator if 'sequence' and 'currentState' properties remain unused
    private class AdvancingSequence(
        val sequence: HomeAnimSequence,
    ) {
        private var index: Int = 0

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