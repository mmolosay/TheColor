package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import io.github.mmolosay.thecolor.utils.doNothing
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

internal class HomeAnimControllerTest {

    lateinit var sut: HomeAnimController

    @Test
    fun `given animation is idling on state 1, when dest states (2) are submitted, then animation '1 → 2' runs correctly`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        sut = HomeAnimController(state1)
        sut.currentState shouldBe state1
        sut.isRunning shouldBe false // idling

        sut.run(destStates = listOf(state2))
        sut.destState shouldBe state2

        sut.onValueReached(value = ColorPreview.Visibility.Visible)
        sut.destState shouldBe state2
        sut.isRunning shouldBe false // finished
    }

    @Test
    fun `given animation is idling on state 1, when dest states (1, 2) are submitted, then animation '1 → 2' runs correctly`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        sut = HomeAnimController(state1)
        sut.currentState shouldBe state1
        sut.isRunning shouldBe false // idling

        sut.run(destStates = listOf(state1, state2))
        sut.destState shouldBe state2

        sut.onValueReached(value = ColorPreview.Visibility.Visible)
        sut.destState shouldBe state2
        sut.isRunning shouldBe false // finished
    }

    @Test
    fun `given animation is idling on state 1, when dest states (1, 2, 3, 4) are submitted, then animation '1 → 2 → 3 → 4' runs correctly`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        val state3 = state2.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        val state4 = state3.copy(colorCenter = ColorCenter.Expanded)
        sut = HomeAnimController(state1)
        sut.currentState shouldBe state1
        sut.isRunning shouldBe false // idling

        sut.run(destStates = listOf(state1, state2, state3, state4))
        sut.destState shouldBe state2

        sut.onValueReached(value = ColorPreview.Visibility.Visible)
        sut.destState shouldBe state3

        sut.onValueReached(value = ColorPreview.Position.Dived)
        sut.destState shouldBe state4

        sut.onValueReached(value = ColorCenter.Expanded)
        sut.destState shouldBe state4
        sut.isRunning shouldBe false // finished
    }

    /**
     * GIVEN
     * [sut] is created with some initial state `1`.
     *
     * WHEN
     * dest states `[1, 1]` are submitted to run
     *
     * THEN
     * SUT recognizes these dest states as no-op and doesn't start animation (or quickly skips it).
     * Thus, [HomeAnimController.isRunning] remains `false`.
     */
    @Test
    fun `given animation is idling on state 1, when dest states (1, 1) are submitted, then animation doesn't start`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        sut = HomeAnimController(state1)
        sut.currentState shouldBe state1
        sut.isRunning shouldBe false // idling

        sut.run(destStates = listOf(state1, state1))

        sut.isRunning shouldBe false // finished or was never started
    }

    @Test
    fun `given segment (1 → 2) is running, when dest states (2, 1) are submitted, then animation '1 → 2 → 1' runs correctly`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        sut = HomeAnimController(state1)

        sut.run(destStates = listOf(state1, state2))
        sut.destState shouldBe state2
        sut.isRunning shouldBe true

        doNothing() // ColorPreview.Visibility -> Visible is still running

        sut.run(destStates = listOf(state2, state1))
        sut.destState shouldBe state2

        sut.onValueReached(value = ColorPreview.Visibility.Visible)
        sut.destState shouldBe state1

        sut.onValueReached(value = ColorPreview.Visibility.Hidden)
        sut.destState shouldBe state1
    }

    /**
     * GIVEN
     * [sut] is created with some initial state `1`.
     *
     * WHEN
     * 1. dest states `[1, 2]` are submitted to run. Some property `X` is different between states `1` and `2`,
     * effectively meaning that the property `X` is being animated in this segment `(1 → 2)`.
     *
     * 2. segment `(1, 2)` has not been even partially executed yet (animation `(1 → 2)` is still ongoing).
     *
     * 3. new dest states `[1]` are submitted to run. Considering that at the moment the segment `(1 → 2)`
     * is running, with new dest states it starts running a segment `(2 → 1)`, which sets dest state to state `1`.
     * As mentioned above, difference in `(1 → 2)` is in property `X`. If the segment `(1 → 2)` has been
     * reversed back to `1` on half way there, that means that property `X` is the one that should be reported
     * as reached in order to confirm that state `1` has been reached.
     *
     * 4. property `Y` is reported as reached. Since it's a part of state `1` but it is
     * NOT THE ONE BEING ANIMATED (different between states `2` and `1`), then
     * it should be ignored and shouldn't be considered as an indicator that state `1` is reached.
     *
     * THEN
     * dest state stays equal to state `1`. It's not reached yet, thus animation doesn't finish.
     */
    @Test
    fun `given segment (1 → 2) is running where value of property X is being animated, when dest states (1) are submitted and value of property Y is reported as reached, then dest state is NOT updated`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Visible,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        sut = HomeAnimController(state1)

        sut.run(destStates = listOf(state1, state2))
        sut.destState shouldBe state2

        doNothing() // ColorPreview.Position -> Dived is still running

        sut.run(destStates = listOf(state1))
        sut.destState shouldBe state1

        // difference between states 2 and 1 is in Color Preview Position, not in Color Preview Visibility
        // so reporting the latter as reached shouldn't be considered as an indicator that state 1 is reached
        sut.onValueReached(value = ColorPreview.Visibility.Visible)
        sut.isRunning shouldBe true // state 1 is not reached, thus animation is still running
        sut.isRunning shouldNotBe false // not finished yet
    }

    @Test
    fun `given segment (2 → 3) is running, when dest states (2, 1) are submitted, then animation '3 → 2 → 1 runs correctly'`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        val state3 = state2.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        sut = HomeAnimController(state2)

        sut.run(destStates = listOf(state2, state3))
        sut.destState shouldBe state3

        doNothing() // ColorPreview.Position -> Dived is still running

        sut.run(destStates = listOf(state2, state1))
        sut.destState shouldBe state2

        sut.onValueReached(value = ColorPreview.Position.NotDived)
        sut.destState shouldBe state1

        sut.onValueReached(value = ColorPreview.Visibility.Hidden)
        sut.destState shouldBe state1
        sut.isRunning shouldBe false // finished
    }

    @Test
    fun `given segment (1 → 2) is running, when dest states (1, 1) are submitted, then animation '1 → 2 → 1' runs correctly`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        sut = HomeAnimController(state1)

        sut.run(destStates = listOf(state1, state2))
        sut.destState shouldBe state2

        doNothing() // ColorPreview.Visibility -> Visible is still running

        sut.run(destStates = listOf(state1, state1))
        sut.destState shouldBe state1

        sut.onValueReached(value = ColorPreview.Visibility.Hidden)
        sut.destState shouldBe state1
        sut.isRunning shouldBe false // finished
    }

    /**
     * GIVEN
     * 1. [sut] is created with some initial state `2`
     * 2. dest states `[2, 3]` are submitted to run, so that segment `(2 → 3)` is running
     *
     * WHEN
     * 1. dest states `[2, 1]` are submitted to run
     * 2. dest states `[2, 3]` are submitted to run
     *
     * THEN
     * Actual animation will look something like
     * `2 → 2.5 → 2.5 → 2 → 3` (`2.5` is a state between `2` and `3`).
     */
    @Test
    fun `given segment (2 → 3) is running, when dest states (2, 1) are submitted and dest states (2, 3) are submitted, then animation runs correctly`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Visible,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        val state3 = state2.copy(colorCenter = ColorCenter.Expanded)
        sut = HomeAnimController(state2)

        sut.run(destStates = listOf(state2, state3))
        sut.destState shouldBe state3

        doNothing() // ColorCenter -> Expanded is still running

        // WHEN #1
        sut.run(destStates = listOf(state2, state1))
        sut.destState shouldBe state2

        doNothing() // ColorCenter -> Collapsed is still running

        // WHEN #2
        sut.run(destStates = listOf(state2, state3))
        sut.destState shouldBe state2

        // THEN
        sut.onValueReached(value = ColorCenter.Collapsed)
        sut.destState shouldBe state3

        sut.onValueReached(value = ColorCenter.Expanded)
        sut.destState shouldBe state3
        sut.isRunning shouldBe false // finished
    }
}