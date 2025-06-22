package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimSequence
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
    fun `run sequence of 2 states`() {
        sut = kotlin.run {
            val initialState = HomeAnimState(
                colorPreviewPosition = ColorPreview.Position.NotDived,
                colorPreviewVisibility = ColorPreview.Visibility.Hidden,
                colorCenter = ColorCenter.Collapsed,
            )
            HomeAnimController(initialState)
        }

        val sequence = buildList {
            sut.currentState
                .also { add(it) }
            last().copy(
                colorPreviewVisibility = ColorPreview.Visibility.Visible,
            ).also { add(it) }
        }.let { states -> HomeAnimSequence(states) }
        sut.run(sequence)
        sut.currentState shouldBe sequence[0]
        sut.destState shouldBe sequence[1]

        sut.reportValueReached(ColorPreview.Visibility.Visible)
        sut.currentState shouldBe sequence[1]
        sut.destState shouldBe sequence[1]
    }

    @Test
    fun `run sequence of 4 states`() {
        sut = kotlin.run {
            val initialState = HomeAnimState(
                colorPreviewPosition = ColorPreview.Position.NotDived,
                colorPreviewVisibility = ColorPreview.Visibility.Hidden,
                colorCenter = ColorCenter.Collapsed,
            )
            HomeAnimController(initialState)
        }

        val sequence = buildList {
            sut.currentState
                .also { add(it) }
            last().copy(
                colorPreviewVisibility = ColorPreview.Visibility.Visible,
            ).also { add(it) }
            last().copy(
                colorPreviewPosition = ColorPreview.Position.Dived,
            ).also { add(it) }
            last().copy(
                colorCenter = ColorCenter.Expanded,
            ).also { add(it) }
        }.let { states -> HomeAnimSequence(states) }
        sut.run(sequence)
        sut.currentState shouldBe sequence[0]
        sut.destState shouldBe sequence[1]

        sut.reportValueReached(ColorPreview.Visibility.Visible)
        sut.currentState shouldBe sequence[1]
        sut.destState shouldBe sequence[2]

        sut.reportValueReached(ColorPreview.Position.Dived)
        sut.currentState shouldBe sequence[2]
        sut.destState shouldBe sequence[3]

        sut.reportValueReached(ColorCenter.Expanded)
        sut.currentState shouldBe sequence[3]
        sut.destState shouldBe sequence[3]
    }

    /**
     * GIVEN
     * [sut] is created with some initial state 1.
     *
     * WHEN
     * sequence [1, 1] is submitted to run
     *
     * THEN
     * SUT recognizes this sequence as no-op and doesn't start animation (or quickly skip it).
     * Thus, [HomeAnimController.isRunning] remains `false`.
     */
    @Test
    fun `given initial state is 1, when sequence '1 → 1' is submitted, then it doesn't start`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        sut = HomeAnimController(state1)

        val sequence = HomeAnimSequence(states = listOf(state1, state1))
        sut.run(sequence)

        sut.isRunning shouldBe false
    }

    /**
     * GIVEN
     * [sut] is created with some initial state 1.
     *
     * WHEN
     * 1. sequence [1, 2] is submitted to run. Some property X is different between states 1 and 2,
     * effectively meaning that the property X is being animated in this segment.
     *
     * 2. segment (1, 2) has not been even partially executed yet (e.g. animation 1 → 2 is still ongoing).
     *
     * 3. new sequence [1, 1] is submitted to run. Considering that at the moment the segment (1, 2)
     * is running, with new sequence it starts running a segment (1, 1), which sets dest state to state 1.
     * As mentioned above, difference in (1, 2) is in property X. If the segment (1, 2) has been
     * reversed back to 1 on half way there, that means that property X is the one that should be reported
     * as reached in order to confirm that state 1 has been returned.
     *
     * 4. property Y is reported as reached. Since it's a part of state 1 but it is
     * NOT THE ONE BEING ANIMATED (different between states 2 and 1), then
     * it should be ignored and shouldn't be considered as an indicator that state 1 is reached.
     *
     * THEN
     * dest state stays equal to state 2. Sequence doesn't finish.
     */
    @Test
    fun `given segment '1 → 2' is running where value of property X is being animated, when new segment (1 → 1) is started and value of property Y is reported as reached, then dest state is NOT updated`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Visible,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        sut = HomeAnimController(state1)

        val sequence12 = HomeAnimSequence(states = listOf(state1, state2))
        sut.run(sequence12)
        sut.currentState shouldBe state1
        sut.destState shouldBe state2

        doNothing() // state 2 hasn't been reached yet (animation is still running)

        val sequence11 = HomeAnimSequence(states = listOf(state1, state1))
        sut.run(sequence11)
        sut.currentState shouldBe state1
        sut.destState shouldBe state1

        // difference between states 2 and 1 is in Color Preview position, not in Color Preview Visibility
        // so reporting the latter as reached shouldn't be considered as an indicator that state 1 is reached
        sut.reportValueReached(ColorPreview.Visibility.Visible)
        sut.isRunning shouldBe true // state 1 is not reached, thus animation is still running
        sut.isRunning shouldNotBe false
    }

    /**
     * See also the test below with sequences in reversed order.
     *
     * GIVEN
     * [sut] is created with some initial state 1.
     *
     * WHEN
     * 1. sequence [1, 2] is submitted to run
     * 2. sequence [1, 2] has not been even partially executed yet (e.g. animation 1 → 2 is still ongoing)
     * 3. new sequence [1, 2, 3] is submitted to run
     *
     * THEN
     * all states from latter sequence [1, 2, 3] are animated.
     */
    @Test
    fun `given initial state is 1, when sequence '1 → 2' is submitted, and then another sequence '1 → 2 → 3' is submitted before first sequence is partially executed, then all states from second sequence are animated`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        val state3 = state2.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        sut = HomeAnimController(state1)

        // WHEN #1
        val sequence12 = HomeAnimSequence(states = listOf(state1, state2))
        sut.run(sequence12)
        sut.destState shouldBe state2
        // WHEN #2
        doNothing() // state 2 hasn't been reached yet (animation is still running)
        // WHEN #3
        val sequence123 = HomeAnimSequence(states = listOf(state1, state2, state3))
        sut.run(sequence123)
        sut.currentState shouldBe state1
        sut.destState shouldBe state2

        sut.reportValueReached(state2.colorPreviewVisibility)
        sut.currentState shouldBe state2
        sut.destState shouldBe state3

        sut.reportValueReached(state3.colorPreviewPosition)
        sut.currentState shouldBe state3
        sut.destState shouldBe state3
        sut.isRunning shouldBe false // finished
    }

    /**
     * See also the test above with sequences in reversed order.
     *
     * GIVEN
     * [sut] is created with some initial state 1.
     *
     * WHEN
     * 1. sequence [1, 2, 3] is submitted to run
     * 2. sequence [1, 2, 3] has not been even partially executed yet (e.g. animation 1 → 2 is still ongoing)
     * 3. new sequence [1, 2] is submitted to run
     *
     * THEN
     * all states from latter sequence [1, 2] are animated.
     */
    @Test
    fun `given initial state is 1, when sequence '1 → 2 → 3' is submitted, and then another sequence '1 → 2' is submitted before first sequence is partially executed, then all states from second sequence are animated`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        val state3 = state2.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        sut = HomeAnimController(state1)

        // WHEN #1
        val sequence123 = HomeAnimSequence(states = listOf(state1, state2, state3))
        sut.run(sequence123)
        sut.destState shouldBe state2
        // WHEN #2
        doNothing() // state 2 hasn't been reached yet (animation is still running)
        // WHEN #3
        val sequence12 = HomeAnimSequence(states = listOf(state1, state2))
        sut.run(sequence12)
        sut.currentState shouldBe state1
        sut.destState shouldBe state2

        sut.reportValueReached(state2.colorPreviewVisibility)
        sut.currentState shouldBe state2
        sut.destState shouldBe state2
        sut.isRunning shouldBe false // finished
    }

    @Test
    fun `when segment '2 → 3' is running and new sequence is '2 → 1', then 2 is not skipped`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        val state3 = state2.copy(colorPreviewPosition = ColorPreview.Position.Dived)
        sut = HomeAnimController(state2)

        val sequence23 = HomeAnimSequence(states = listOf(state2, state3))
        sut.run(sequence23)
        sut.currentState shouldBe state2
        sut.destState shouldBe state3

        doNothing() // state 3 hasn't been reached yet (animation is still running)

        val sequence21 = HomeAnimSequence(states = listOf(state2, state1))
        sut.run(sequence21)
        sut.currentState shouldBe state2
        sut.destState shouldBe state2

        sut.reportValueReached(ColorPreview.Position.NotDived)
        sut.currentState shouldBe state2
        sut.destState shouldBe state1

        sut.reportValueReached(ColorPreview.Visibility.Hidden)
        sut.currentState shouldBe state1
        sut.isRunning shouldBe false
    }

    @Test
    fun `when segment '1 → 2' is running and new sequence is '1 → 1', then animation back towards 1 starts`() {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        sut = HomeAnimController(state1)

        val sequence12 = HomeAnimSequence(states = listOf(state1, state2))
        sut.run(sequence12)
        sut.currentState shouldBe state1
        sut.destState shouldBe state2

        doNothing()// reached state is not reported, thus animation is still running

        val sequence11 = HomeAnimSequence(states = listOf(state1, state1))
        sut.run(sequence11)

        sut.reportValueReached(state1.colorPreviewVisibility) // state1 is reached
        sut.currentState shouldBe state1
        sut.destState shouldBe state1
        sut.isRunning shouldBe false
    }
}