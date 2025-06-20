package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimSequence
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState.ColorPreview
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

internal class HomeAnimControllerTest {

    lateinit var sut: HomeAnimController

    /**
     * GIVEN
     * [sut] is created with some initial state X.
     *
     * WHEN
     * sequence [X, X] is submitted to run
     *
     * THEN
     * SUT recognizes this sequence as no-op and doesn't start animation.
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

    // TODO: finish me? Was added to test/debug an actual behaviour of SUT in a controlled manner
    @Test
    fun `whaaa`() = runTest {
        sut = kotlin.run {
            val initialState = HomeAnimState(
                colorPreviewPosition = ColorPreview.Position.NotDived,
                colorPreviewVisibility = ColorPreview.Visibility.Hidden,
                colorCenter = ColorCenter.Collapsed,
            )
            HomeAnimController(initialState)
        }

        kotlin.run makeColorPreviewVisible@{
            val sequence = buildList {
                sut.currentState
                    .also { add(it) }
                last().copy(
                    colorPreviewVisibility = ColorPreview.Visibility.Visible,
                ).also { add(it) }
            }.let { states -> HomeAnimSequence(states) }
            sut.run(sequence)
        }

        // until it's not finished
        kotlin.run runFullForwardSequence@{
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
        }
        sut.reportDestReached(ColorPreview.Visibility.Visible) // first dest is reached

        // e.g. proceeded color changes
        kotlin.run cancelOngoingSequence@{
            val currentState = sut.currentState
            val sequence = HomeAnimSequence(states = listOf(currentState, currentState))
            sut.run(sequence)
        }

        // until it's not finished
        kotlin.run runFullForwardSequence@{
            val sequence = buildList {
                sut.currentState
                    .also { add(it) }
                last().copy(
                    colorPreviewPosition = ColorPreview.Position.Dived,
                ).also { add(it) }
                last().copy(
                    colorCenter = ColorCenter.Expanded,
                ).also { add(it) }
            }.let { states -> HomeAnimSequence(states) }
            sut.run(sequence)
        }
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
        sut.reportDestReached(ColorPreview.Visibility.Visible)

        sut.currentState shouldBe sequence[1]
        sut.flowOfDestState.value shouldBe sequence[2]
        delay(1.seconds)
        sut.reportDestReached(ColorPreview.Position.Dived)

        sut.currentState shouldBe sequence[2]
        sut.flowOfDestState.value shouldBe sequence[3]
        delay(1.seconds)
        sut.reportDestReached(ColorCenter.Expanded)

        sut.currentState shouldBe sequence[3]
        sut.flowOfDestState.value shouldBe sequence[3]
    }

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
        sut.reportDestReached(ColorPreview.Visibility.Visible)

        sut.currentState shouldBe sequence[1]
        sut.flowOfDestState.value shouldBe sequence[1]
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
        // reached state is not reported, thus animation is still running
        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state2
        sut.run(sequence = HomeAnimSequence(states = listOf(state1, state1)))
        sut.reportDestReached(state1.colorPreviewVisibility) // state1 is reached

        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state1
    }
}