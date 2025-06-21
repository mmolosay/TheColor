package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimSequence
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import io.github.mmolosay.thecolor.utils.doNothing
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

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
        delay(1.seconds)
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
        delay(1.seconds)
        sut.reportStateReached(ColorPreview.Visibility.Visible)

        sut.currentState shouldBe sequence[1]
        sut.flowOfDestState.value shouldBe sequence[2]
        delay(1.seconds)
        sut.reportStateReached(ColorPreview.Position.Dived)

        sut.currentState shouldBe sequence[2]
        sut.flowOfDestState.value shouldBe sequence[3]
        delay(1.seconds)
        sut.reportStateReached(ColorCenter.Expanded)

        sut.currentState shouldBe sequence[3]
        sut.flowOfDestState.value shouldBe sequence[3]
    }

    /**
     * GIVEN
     * [sut] is created with some initial state X.
     *
     * WHEN
     * sequence [X, X] is submitted to run
     *
     * THEN
     * SUT recognizes this sequence as no-op and doesn't start animation (or quickly skip it).
     * Thus, [HomeAnimController.isRunning] remains `false`.
     */
    @Test
    fun `given initial state is X, when sequence 'X → X' is submitted, then it doesn't start`() = runTest {
        val initialState = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        sut = HomeAnimController(initialState)

        val sequence = HomeAnimSequence(states = listOf(initialState, initialState))
        sut.run(sequence)

        sut.isRunning shouldBe false
    }

    /**
     * GIVEN
     * [sut] is created with some initial state X.
     *
     * WHEN
     * 1. sequence [X, X] is submitted to run
     * 2. sequence [X, X] has not been even partially executed yet (e.g. animation X → Y is still ongoing)
     * 3. new sequence [X, Y, Z] is submitted to run
     *
     * THEN
     * all states from latter sequence [X, Y, Z] are animated.
     */
    @Test
    fun `given initial state is X, when sequence 'X → Y' is submitted, and then another sequence 'X → Y → Z' is submitted before first sequence is partially executed, then all states from second sequence are animated`() = runTest {
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
        delay(1.seconds)
        sut.reportStateReached(state2.colorPreviewVisibility)

        sut.currentState shouldBe state2
        sut.flowOfDestState.value shouldBe state3
        sut.reportStateReached(state3.colorPreviewPosition)

        sut.currentState shouldBe state3
        sut.flowOfDestState.value shouldBe state3
        sut.isRunning shouldBe false // finished
    }

    @Test
    fun `when animation 'X → Y' is running and new sequence is 'X → X', then animation back towards X starts`() = runTest {
        val state1 = HomeAnimState(
            colorPreviewPosition = ColorPreview.Position.NotDived,
            colorPreviewVisibility = ColorPreview.Visibility.Hidden,
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 = state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        sut = HomeAnimController(state1)

        sut.run(sequence = HomeAnimSequence(states = listOf(state1, state2)))
        doNothing()// reached state is not reported, thus animation is still running
        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state2
        sut.run(sequence = HomeAnimSequence(states = listOf(state1, state1)))
        sut.reportStateReached(state1.colorPreviewVisibility) // state1 is reached

        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state1
    }
}