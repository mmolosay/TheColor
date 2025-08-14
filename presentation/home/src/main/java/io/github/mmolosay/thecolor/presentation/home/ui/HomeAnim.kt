package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import io.github.mmolosay.thecolor.utils.removeSubsequentDuplicates
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

/**
 * A sequence of [HomeAnimState]s that defines an animation to run.
 *
 * A valid sequence must consist of at least 2 states: start (current state) and at least
 * one dest state to animate to.
 */
private class HomeAnimSequence(
    private val states: List<HomeAnimState>,
) : List<HomeAnimState> by states {

    init {
        require(states.size >= 2) { "Sequence must have at least 2 states: start and finish" }
    }

    // inheritance 'by' delegate doesn't inherit methods of 'Any'
    override fun toString(): String =
        states.toString()
}

/**
 * The whole (full) sequence of the 'Home' animation.
 * Any [HomeAnimSequence] is a sub-sequence of this one. Reverse order is allowed.
 */
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

/**
 * Returns a list of dest states (key frames) that should be animated to when starting from
 * the [from] state and finishing at the [to] state.
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

@Stable
internal class HomeAnimController(
    currentState: HomeAnimState,
) {
    private var lastReachedState: HomeAnimState = currentState

    val flowOfDestState = MutableStateFlow(currentState)
    val destState: HomeAnimState
        get() = flowOfDestState.value

    private var runningSegment: Segment? = null

    private val pendingDests = mutableMapOf<AnimComponent, Any>() // type -> anim dest

    private var runningSequence: RunningSequence? = null

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
        val sequence = normalizeDestStatesToSequence(destStates)
        Timber.d("HomeAnimLog | sequence = $sequence")
        runningSequence = RunningSequence(sequence)
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

    /**
     * Same sequence may have different meanings depending on the current state of animation.
     * We need to adjust (normalize) submitted, "raw" [sequence] according to the current state of
     * animation to make the [sequence] easy to execute in segments.
     */
    private fun normalizeDestStatesToSequence(destStates: List<HomeAnimState>): HomeAnimSequence {
        val newStates = destStates
            .removeSubsequentDuplicates()
            .toMutableList()
        val firstDest = newStates.first()
        val containsSingleDest = (newStates.size == 1)
        val firstDestIsOngoing = if (isRunning) {
            (requireNotNull(runningSegment).dest == firstDest)
        } else false
        val goesBackToOngoingStart = if (isRunning) {
            (requireNotNull(runningSegment).start == firstDest)
        } else false
        when {
            containsSingleDest -> {
                newStates.add(index = 0, element = currentState)
            }
            firstDestIsOngoing -> {
                newStates.add(index = 0, element = requireNotNull(runningSegment).start)
            }
            goesBackToOngoingStart -> {
                newStates.add(index = 0, element = requireNotNull(runningSegment).dest)
            }
        }
        return HomeAnimSequence(newStates)
    }

    private fun setNextDestFromSequence() {
        if (!isRunning) return
        val runningSequence = requireNotNull(runningSequence)
        val segmentToRun = runningSequence.segment()
        if (segmentToRun == null) {
            Timber.d("HomeAnimLog | sequence is finished")
            this.runningSegment = null
            this.runningSequence = null // sequence is finished
            return
        }
        if (segmentToRun.dest != destState) {
            Timber.d("HomeAnimLog | applying segment's dest: ${segmentToRun.dest}")
            // TODO: don't store pendingDests but calc them from currentSegment?
            pendingDests.putAll(destState diffTo segmentToRun.dest) // 'destState' may not have been reached yet, so "snap" to it and calc diff from it
            runningSegment = Segment(start = destState, dest = segmentToRun.dest)
            flowOfDestState.value = segmentToRun.dest
            return
        }
        if (segmentToRun.dest == destState && pendingDests.isNotEmpty()) {
            Timber.d("HomeAnimLog | identical segment is already running: $segmentToRun")
            return // identical segment is already running
        }
        if (segmentToRun.dest == destState || segmentToRun.isEmpty()) {
            Timber.d("HomeAnimLog | skipping segment, its diff: ${segmentToRun.asDiff()}")
            runningSequence.advance()
            setNextDestFromSequence()
            return
        }
        error("None of the sequence advancing conditions were met")
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

    private class RunningSequence(
        val sequence: HomeAnimSequence,
    ) {
        private var index: Int = 0

        fun segment(): Segment? {
            val currentState = sequence.getOrNull(index) ?: return null
            val nextState = sequence.getOrNull(index + 1) ?: return null
            return Segment(start = currentState, dest = nextState)
        }

        fun advance() {
            index++
        }
    }

    private data class Segment(
        val start: HomeAnimState,
        val dest: HomeAnimState,
    )

    private fun Segment.isEmpty(): Boolean =
        (start == dest)

    private fun Segment.isContiguousWith(other: Segment): Boolean =
        (this.dest == other.start)

    private fun Segment.asDiff(): Map<AnimComponent, Any> =
        start diffTo dest

    private fun List<HomeAnimState>.segmentAt(index: Int): Segment? {
        val start = this.getOrNull(index) ?: return null
        val dest = this.getOrNull(index + 1) ?: return null
        return Segment(start, dest)
    }

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