package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import kotlinx.coroutines.flow.MutableStateFlow
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

/**
 * The whole (full) sequence of the 'Home' animation.
 * Any list of dest states is a sub-sequence of this one. Reverse order is allowed.
 */
private val FullForwardSequence: List<HomeAnimState> = run {
    val states = buildList {
        // 0
        HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        ).also { add(it) }
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
    val expectedLast = HomeAnimState(
        colorPreviewPosition = ColorPreview.Position.Dived,
        colorPreviewVisibility = ColorPreview.Visibility.Visible,
        colorCenter = ColorCenter.Expanded,
    )
    assert(states.last() == expectedLast)
    return@run states
}

/**
 * Returns a list of dest states (key frames) that should be animated to when starting from
 * the [from] state and finishing at the [to] state.
 *
 * Resulting list contains [from], all intermediate states in between, and [to].
 * Contains only one state if [from] and [to] are identical.
 */
internal fun makeDestStates(
    from: HomeAnimState,
    to: HomeAnimState,
): List<HomeAnimState> {
    require(from in FullForwardSequence)
    require(to in FullForwardSequence)
    val indexOfCurrent = FullForwardSequence.indexOf(from) // -1 is impossible due to 'contains()' check above
    val indexOfDest = FullForwardSequence.indexOf(to) // -1 is impossible due to 'contains()' check above
    return when {
        indexOfCurrent < indexOfDest ->
            FullForwardSequence.subList(indexOfCurrent, indexOfDest + 1)
        indexOfCurrent > indexOfDest ->
            FullForwardSequence.subList(indexOfDest, indexOfCurrent + 1).reversed()
        else -> {
            assert(indexOfCurrent == indexOfDest)
            assert(from == to)
            listOf(to)
        }
    }
}

internal fun makeDestStates(
    ongoingStart: HomeAnimState,
    ongoingDest: HomeAnimState,
    to: HomeAnimState,
): List<HomeAnimState> {
    val destStates = makeDestStates(from = ongoingStart, to = to).toMutableList()
    if (destStates.size > 1 && destStates[0] == ongoingStart && destStates[1] == ongoingDest) {
        destStates.removeAt(index = 0)
    }
    return destStates
}

@Stable
internal class HomeAnimController(
    currentState: HomeAnimState,
) {
    private var lastReachedState: HomeAnimState = currentState

    val flowOfDestState = MutableStateFlow(currentState)
    val destState: HomeAnimState
        get() = flowOfDestState.value

    var runningSegment: Segment? = null
        private set

    private val pendingDests = mutableMapOf<AnimComponent, Any>() // type -> anim dest

    private var runningSequence: Sequence? = null

    val isRunning: Boolean
        get() = (runningSequence != null)

    val currentState: HomeAnimState
        get() = if (isRunning) requireNotNull(runningSegment).start else lastReachedState

    /**
     * Runs the specified animation.
     * List if [destStates] defines the key frames that should be animated to.
     */
    fun run(destStates: List<HomeAnimState>) {
        Timber.d("HomeAnimLog | run(), destStates = $destStates, lastReachedState = $lastReachedState, currentSegment = $runningSegment")
        require(destStates.isNotEmpty()) { "Dest states must have at least one state" }
        Timber.d("HomeAnimLog | sequence = $sequence")
        runningSequence = Sequence(destStates)
        setNextDestFromSequence()
    }

    fun onValueReached(value: ColorPreview.Position) =
        onValueReached(
            reachedValue = value,
            destValue = destState.colorPreviewPosition,
            component = AnimComponent.ColorPreviewPosition,
            applyToCurrentState = { it.copy(colorPreviewPosition = value) },
        )

    fun onValueReached(value: ColorPreview.Visibility) =
        onValueReached(
            reachedValue = value,
            destValue = destState.colorPreviewVisibility,
            component = AnimComponent.ColorPreviewVisibility,
            applyToCurrentState = { it.copy(colorPreviewVisibility = value) },
        )

    fun onValueReached(value: ColorCenter) =
        onValueReached(
            reachedValue = value,
            destValue = destState.colorCenter,
            component = AnimComponent.ColorCenter,
            applyToCurrentState = { it.copy(colorCenter = value) },
        )

    private fun <T : Any> onValueReached(
        reachedValue: T,
        destValue: T,
        component: AnimComponent,
        applyToCurrentState: (HomeAnimState) -> HomeAnimState,
    ) {
        if (!isRunning) return
        if (reachedValue != destValue) return
        val isExpectedToBeReached = kotlin.run {
            @Suppress("UNCHECKED_CAST")
            val pendingDest = pendingDests[component] as T?
            (reachedValue == pendingDest)
        }
        if (!isExpectedToBeReached) return
        assert(reachedValue == destValue)
        assert(isExpectedToBeReached)
        Timber.d("HomeAnimLog | onValueReached(), reachedValue = $reachedValue, destValue = $destValue, component = $component")
        lastReachedState = applyToCurrentState(lastReachedState)
        pendingDests.remove(component)
        checkIfDestIsReachedAndSetNext()
    }

    private fun setNextDestFromSequence() {
        if (!isRunning) return
        val runningSequence = requireNotNull(runningSequence)
        val nextDest = runningSequence.nextDest()
        if (nextDest == null) {
            this.runningSegment = null
            this.runningSequence = null // sequence is finished
            return
        }
        if (nextDest != destState) {
            pendingDests.putAll(destState diffTo nextDest) // 'destState' may not have been reached yet, so "snap" to it and calc diff from it
            runningSegment = Segment(start = destState, dest = nextDest)
            flowOfDestState.value = nextDest
            return
        }
        if (nextDest == destState && pendingDests.isNotEmpty()) {
            return // identical segment is already running
        }
        if (nextDest == destState) {
            runningSequence.advance()
            setNextDestFromSequence()
            return
        }
        error("The sequence advancing condition was not met")
    }

    private fun checkIfDestIsReachedAndSetNext() {
        if (!isRunning) return
        if (lastReachedState == destState && pendingDests.isEmpty()) {
            requireNotNull(runningSequence).advance()
            setNextDestFromSequence()
        }
    }

    private infix fun HomeAnimState.diffTo(next: HomeAnimState): Map<AnimComponent, Any> {
        fun <T> componentDiff(value: (HomeAnimState) -> T): T? =
            value(next).takeIf { it != value(this) }
        return buildMap {
            componentDiff { it.colorPreviewPosition }?.let {
                this[AnimComponent.ColorPreviewPosition] = it
            }
            componentDiff { it.colorPreviewVisibility }?.let {
                this[AnimComponent.ColorPreviewVisibility] = it
            }
            componentDiff { it.colorCenter }?.let {
                this[AnimComponent.ColorCenter] = it
            }
        }
    }

    private class Sequence(
        val destStates: List<HomeAnimState>,
    ) {
        private var indexOfNext = 0

        fun nextDest(): HomeAnimState? =
            destStates.getOrElse(index = indexOfNext) { return null }

        fun advance() {
            indexOfNext++
        }
    }

    data class Segment(
        val start: HomeAnimState,
        val dest: HomeAnimState,
    )

    private enum class AnimComponent {
        ColorPreviewPosition,
        ColorPreviewVisibility,
        ColorCenter,
    }
}

/**
 * 'Color Center's focal point is a point on the screen that is the center of the clipping circle
 * in its circular reveal animation.
 * It is also a point where 'Color Preview' dives to and rises from during its animation.
 */
internal val ColorCenterFocalPointBottomOffset = 96.dp