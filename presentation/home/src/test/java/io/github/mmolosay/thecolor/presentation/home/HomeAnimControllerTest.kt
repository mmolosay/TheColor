package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimSequence
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import io.github.mmolosay.thecolor.utils.doNothing
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class HomeAnimControllerTest {

    lateinit var sut: HomeAnimController

    @Test
    fun `run sequence of 2 states`() = runTest {
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
        sut.flowOfDestState.value shouldBe sequence[1]

        sut.reportStateReached(ColorPreview.Visibility.Visible)
        sut.currentState shouldBe sequence[1]
        sut.flowOfDestState.value shouldBe sequence[1]
    }

    @Test
    fun `run sequence of 4 states`() = runTest {
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
        sut.flowOfDestState.value shouldBe sequence[1]

        sut.reportStateReached(ColorPreview.Visibility.Visible)
        sut.currentState shouldBe sequence[1]
        sut.flowOfDestState.value shouldBe sequence[2]

        sut.reportStateReached(ColorPreview.Position.Dived)
        sut.currentState shouldBe sequence[2]
        sut.flowOfDestState.value shouldBe sequence[3]

        sut.reportStateReached(ColorCenter.Expanded)
        sut.currentState shouldBe sequence[3]
        sut.flowOfDestState.value shouldBe sequence[3]
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
    fun `given initial state is 1, when sequence '1 → 1' is submitted, then it doesn't start`() = runTest {
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
    fun `given initial state is 1, when sequence '1 → 2' is submitted, and then another sequence '1 → 2 → 3' is submitted before first sequence is partially executed, then all states from second sequence are animated`() = runTest {
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
        sut.flowOfDestState.value shouldBe state2
        // WHEN #2
        doNothing() // state 2 hasn't been reached yet (animation is still running)
        // WHEN #3
        val sequence123 = HomeAnimSequence(states = listOf(state1, state2, state3))
        sut.run(sequence123)
        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state2

        sut.reportStateReached(state2.colorPreviewVisibility)
        sut.currentState shouldBe state2
        sut.flowOfDestState.value shouldBe state3

        sut.reportStateReached(state3.colorPreviewPosition)
        sut.currentState shouldBe state3
        sut.flowOfDestState.value shouldBe state3
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
    fun `given initial state is 1, when sequence '1 → 2 → 3' is submitted, and then another sequence '1 → 2' is submitted before first sequence is partially executed, then all states from second sequence are animated`() = runTest {
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
        sut.flowOfDestState.value shouldBe state2
        // WHEN #2
        doNothing() // state 2 hasn't been reached yet (animation is still running)
        // WHEN #3
        val sequence12 = HomeAnimSequence(states = listOf(state1, state2))
        sut.run(sequence12)
        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state2

        sut.reportStateReached(state2.colorPreviewVisibility)
        sut.currentState shouldBe state2
        sut.flowOfDestState.value shouldBe state2
        sut.isRunning shouldBe false // finished
    }

    @Test
    fun `when animation '2 → 3' is running and new sequence is '2 → 1', then 2 is not skipped`() = runTest {
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
        sut.flowOfDestState.value shouldBe state3

        doNothing() // state 3 hasn't been reached yet (animation is still running)

        val sequence21 = HomeAnimSequence(states = listOf(state2, state1))
        sut.run(sequence21)
        sut.currentState shouldBe state2
        sut.flowOfDestState.value shouldBe state2

        sut.reportStateReached(ColorPreview.Position.NotDived)
        sut.currentState shouldBe state2
        sut.flowOfDestState.value shouldBe state1

        sut.reportStateReached(ColorPreview.Visibility.Hidden)
        sut.currentState shouldBe state1
        sut.isRunning shouldBe false
    }

    @Test
    fun `when animation '1 → 2' is running and new sequence is '1 → 1', then animation back towards 1 starts`() = runTest {
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
        sut.flowOfDestState.value shouldBe state2

        doNothing()// reached state is not reported, thus animation is still running

        val sequence11 = HomeAnimSequence(states = listOf(state1, state1))
        sut.run(sequence11)

        sut.reportStateReached(state1.colorPreviewVisibility) // state1 is reached
        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state1
        sut.isRunning shouldBe false
    }
}