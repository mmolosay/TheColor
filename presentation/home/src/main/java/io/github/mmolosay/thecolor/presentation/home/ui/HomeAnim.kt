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

// TODO: abolish type? Replace with typealias?
internal class HomeAnimSequence(states: List<HomeAnimState>) : List<HomeAnimState> by states {

    // TODO: rework? extract into List utils?
    fun HomeAnimState.next(): HomeAnimState? {
        require(this in this@HomeAnimSequence) // state in sequence
        val indexOfState = indexOf(this) // can't be -1 because of contains() check above ↑
        return getOrNull(indexOfState + 1)
    }
}

private val FullForwardSequence: HomeAnimSequence = run {
    val states = buildList {
        HomeAnimStates.Collapsed
            .also { add(it) }
        last().copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
            .also { add(it) }
        last().copy(colorPreviewPosition = ColorPreview.Position.Dived)
            .also { add(it) }
        last().copy(colorCenter = ColorCenter.Expanded)
            .also { add(it) }
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
    val indexOfCurrent = FullForwardSequence.indexOf(from)
    val indexOfDest = FullForwardSequence.indexOf(to)
    val subsequence = if (indexOfCurrent <= indexOfDest) {
        FullForwardSequence.subList(indexOfCurrent, indexOfDest + 1)
    } else {
        FullForwardSequence.subList(indexOfDest, indexOfCurrent + 1).reversed()
    }
    require(subsequence.isNotEmpty()) { "Cannot make 'HomeAnimSequence' from $from to $to" }
    return HomeAnimSequence(subsequence)
}

// TODO: ADD UNIT TESTS
internal class HomeAnimController(
    currentState: HomeAnimState,
) {
    // key state is a state that occurs in started sequence; like a key frame
    private var lastReachedKeyState: HomeAnimState = currentState
    var currentState: HomeAnimState = currentState
    val flowOfDestState = MutableStateFlow(currentState)

    private var sequence: HomeAnimSequence? = null
    private var isRunning = false

    /** Starts animation of [sequence] until the end of it. */
    fun run(sequence: HomeAnimSequence) {
        require(sequence.isNotEmpty()) { "can't animate empty sequence" }
        require(sequence.first() == currentState) // sequence must start from current state
        if (sequence.size == 1) {
            if (!isRunning) return // nothing to animate
            // animating from half-finished dest back to current state
            if ((sequence.single() == currentState) && isRunning) {
                this.sequence = sequence
                flowOfDestState.value = sequence.single()
            }
        } else {
            this.sequence = sequence
            flowOfDestState.value = requireNotNull(nextInSequence())
            isRunning = true
        }
    }

    fun reportDestReached(dest: ColorPreview.Position) {
        if (!isRunning) return
        currentState = currentState.copy(colorPreviewPosition = dest)
        checkIfDestIsReachedAndSetNext()
    }

    fun reportDestReached(dest: ColorPreview.Visibility) {
        if (!isRunning) return
        currentState = currentState.copy(colorPreviewVisibility = dest)
        checkIfDestIsReachedAndSetNext()
    }

    fun reportDestReached(dest: ColorCenter) {
        if (!isRunning) return
        currentState = currentState.copy(colorCenter = dest)
        checkIfDestIsReachedAndSetNext()
    }

    private fun nextInSequence(): HomeAnimState? {
        val sequence = requireNotNull(sequence)
        require(lastReachedKeyState in sequence)
        return with(sequence) { lastReachedKeyState.next() }
    }

    private fun checkIfDestIsReachedAndSetNext() {
        val currentDest = flowOfDestState.value
        if (currentState != currentDest) return
        assert(currentState == currentDest)
        lastReachedKeyState = currentState

        if (!isRunning) return // if running, update next dest
        val nextDest = nextInSequence()
        if (nextDest != null) {
            flowOfDestState.value = nextDest
        } else {
            isRunning = false
            this.sequence = null
        }
    }
}

/**
 * 'Color Center's focal point is a point on the screen that is the center of the clipping circle
 * in its circular reveal animation.
 * It is also a point where 'Color Preview' dives to and rises from during its animation.
 */
internal val ColorCenterFocalPointBottomOffset = 96.dp