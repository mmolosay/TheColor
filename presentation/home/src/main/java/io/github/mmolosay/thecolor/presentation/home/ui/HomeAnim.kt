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

internal class HomeAnimController(
    currentState: HomeAnimState,
) {
    var currentState: HomeAnimState = currentState
        private set

    val flowOfDestState = MutableStateFlow(currentState)

    val destState: HomeAnimState
        get() = flowOfDestState.value

    private val currentSegment: Segment
        get() = Segment(start = currentState, dest = destState)

    private val pendingDests = mutableMapOf<AnimComponent, Any>() // type -> anim dest

    private var runningSequence: RunningSequence? = null

    val isRunning: Boolean
        get() = (runningSequence != null)

    fun run(sequence: HomeAnimSequence) {
        require(sequence.first() == currentState) { "sequence must start from current state" }
        val adjustedSequence = normalizeSubmittedSequence(sequence)
        runningSequence = RunningSequence(adjustedSequence)
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
        currentState = applyToCurrentState(currentState)
        pendingDests.remove(component)
        checkIfDestIsReachedAndSetNext()
    }

    /**
     * Same sequence may have different meanings depending on the current state of animation.
     * We need to adjust (normalize) submitted, "raw" [sequence] according to the current state of
     * animation to make the [sequence] easy to execute in segments.
     */
    private fun normalizeSubmittedSequence(sequence: HomeAnimSequence): HomeAnimSequence {
        if (!isRunning) return sequence
        assert(isRunning == true)
        val newStates = sequence.toMutableList()
        val firstSegment = Segment(start = sequence[0], dest = sequence[1])
        // if current == (2, 3) && first == (2, 1), then make first == (2, 2) to reverse current (2, 3)
        kotlin.run {
            val currentAndFirstStartFromSameButFinishOnDiffStates =
                (currentSegment.start == firstSegment.start) && (currentSegment.dest != firstSegment.dest)
            if (currentAndFirstStartFromSameButFinishOnDiffStates && firstSegment.isEmpty().not()) {
                // before animating to firstSegment.dest, animate back to firstSegment.start
                newStates.add(index = 1, element = currentSegment.start)
            }
        }
        return HomeAnimSequence(newStates)
    }

    private fun setNextDestFromSequence() {
        if (!isRunning) return
        val runningSequence = requireNotNull(runningSequence)
        val segmentToRun = runningSequence.segment()
        if (segmentToRun == null) {
            this.runningSequence = null // sequence is finished
            return
        }
        require(segmentToRun.start == currentState)
        if (segmentToRun.dest != destState) {
            pendingDests.putAll(destState diffTo segmentToRun.dest)
            flowOfDestState.value = segmentToRun.dest
            return
        }
        if (currentSegment == segmentToRun && pendingDests.isNotEmpty()) {
            return // identical segment is already running
        }
        if (segmentToRun.dest == destState || segmentToRun.isEmpty()) {
            runningSequence.advance()
            setNextDestFromSequence()
            return
        }
    }

    private fun checkIfDestIsReachedAndSetNext() {
        if (!isRunning) return
        if (currentState == destState && pendingDests.isEmpty()) {
            requireNotNull(runningSequence).advance()
            setNextDestFromSequence()
        }
    }

    private infix fun HomeAnimState.diffTo(next: HomeAnimState): Map<AnimComponent, Any> {
        fun <T> componentDiff(value: (HomeAnimState) -> T): T? =
            if (value(next) != value(this)) value(next) else null
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