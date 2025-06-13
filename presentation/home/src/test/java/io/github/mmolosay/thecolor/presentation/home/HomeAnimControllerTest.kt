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

    @Test
    fun `run sequence of 4 states`() = runTest {
        sut = kotlin.run {
            val initialState = HomeAnimState(
                colorPreview = ColorPreview(
                    position = ColorPreview.Position.NotDived,
                    visibility = ColorPreview.Visibility.Hidden,
                ),
                colorCenter = ColorCenter.Collapsed,
            )
            HomeAnimController(initialState)
        }

        val sequence = buildList {
            sut.currentState
                .also { add(it) }
            last().copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
                .also { add(it) }
            last().copy(colorPreviewPosition = ColorPreview.Position.Dived)
                .also { add(it) }
            last().copy(colorCenter = ColorCenter.Expanded)
                .also { add(it) }
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
                colorPreview = ColorPreview(
                    position = ColorPreview.Position.NotDived,
                    visibility = ColorPreview.Visibility.Hidden,
                ),
                colorCenter = ColorCenter.Collapsed,
            )
            HomeAnimController(initialState)
        }

        val sequence = buildList {
            sut.currentState
                .also { add(it) }
            last().copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
                .also { add(it) }
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
    fun `when animation is not running and sequence has single state, then nothing happens`() = runTest {
        sut = kotlin.run {
            val initialState = HomeAnimState(
                colorPreview = ColorPreview(
                    position = ColorPreview.Position.NotDived,
                    visibility = ColorPreview.Visibility.Hidden,
                ),
                colorCenter = ColorCenter.Collapsed,
            )
            HomeAnimController(initialState)
        }
        val initialCurrentState = sut.currentState
        val initialDestState = sut.flowOfDestState.value

        val sequence = HomeAnimSequence(states = listOf(sut.currentState))
        sut.run(sequence)

        sut.currentState shouldBe initialCurrentState
        sut.flowOfDestState.value shouldBe initialDestState
    }

    @Test
    fun `when animation 'X → Y' is running and sequence has single X state, then animation back towards X starts`() = runTest {
        val state1 = HomeAnimState(
            colorPreview = ColorPreview(
                position = ColorPreview.Position.NotDived,
                visibility = ColorPreview.Visibility.Hidden,
            ),
            colorCenter = ColorCenter.Collapsed,
        )
        val state2 =  state1.copy(colorPreviewVisibility = ColorPreview.Visibility.Visible)
        sut = HomeAnimController(state1)

        sut.run(sequence = HomeAnimSequence(states = listOf(state1, state2)))
        // reached state is not reported, thus animation is still running
        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state2
        sut.run(sequence = HomeAnimSequence(states = listOf(state1)))
        sut.reportDestReached(state1.colorPreview.visibility) // state1 is reached

        sut.currentState shouldBe state1
        sut.flowOfDestState.value shouldBe state1
    }
}