package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvAction
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvState
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvViewModel
import io.github.mmolosay.thecolor.utils.Atom
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputHsvViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var sut: ColorInputHsvViewModel

    @Test
    fun `when 'SetHue' action is executed, then only the hue of the shown color changes`() =
        runTest(testDispatcher) {
            val stateFlow = run {
                val color = Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f)
                MutableStateFlow(MockColorInputStates.ColorInputHsvState(color))
            }
            createSut(stateFlow)

            sut.execute(ColorInputHsvAction.SetHue(hue = 240f))

            stateFlow.value.displayColor shouldBe Color.Hsv(hue = 240f, saturation = 0.5f, value = 0.25f)
        }

    @Test
    fun `when 'SetSaturationAndValue' action is executed, then only the saturation and value of the shown color change`() =
        runTest(testDispatcher) {
            val stateFlow = run {
                val color = Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f)
                MutableStateFlow(MockColorInputStates.ColorInputHsvState(color))
            }
            createSut(stateFlow)

            sut.execute(ColorInputHsvAction.SetSaturationAndValue(saturation = 0.75f, value = 1f))

            stateFlow.value.displayColor shouldBe Color.Hsv(hue = 120f, saturation = 0.75f, value = 1f)
        }

    @Test
    fun `given there is no color, when 'SetHue' action is executed, then saturation and value are those of the pickers' starting position`() =
        runTest(testDispatcher) {
            val stateFlow = MutableStateFlow(MockColorInputStates.ColorInputHsvState(color = null))
            createSut(stateFlow)

            sut.execute(ColorInputHsvAction.SetHue(hue = 240f))

            stateFlow.value.displayColor shouldBe Color.Hsv(hue = 240f, saturation = 1f, value = 1f)
        }

    /**
     * Tests that an action brings only the component the user moved,
     * so that it can't bring back the other components of a color that has been replaced.
     */
    @Test
    fun `given the shown color was set elsewhere, when 'SetHue' action is executed, then saturation and value of that color are kept`() =
        runTest(testDispatcher) {
            val stateFlow = run {
                val color = Color.Hsv(hue = 120f, saturation = 0.5f, value = 0.25f)
                MutableStateFlow(MockColorInputStates.ColorInputHsvState(color))
            }
            createSut(stateFlow)
            run setColorElsewhere@{
                val color = Color.Hsv(hue = 10f, saturation = 0.2f, value = 0.9f)
                stateFlow.value = MockColorInputStates.ColorInputHsvState(color)
            }

            sut.execute(ColorInputHsvAction.SetHue(hue = 240f))

            stateFlow.value.displayColor shouldBe Color.Hsv(hue = 240f, saturation = 0.2f, value = 0.9f)
        }

    @Test
    fun `given a color was sampled, when the pickers move again within the sampling period, then the new color is applied once the period has passed`() =
        runTest(testDispatcher) {
            val stateFlow = MutableStateFlow(MockColorInputStates.ColorInputHsvState(color = null))
            createSut(stateFlow)
            sut.execute(ColorInputHsvAction.SetHue(hue = 240f)) // the first sample is produced at once
            stateFlow.value.color shouldBe stateFlow.value.displayColor // assumption of the test

            sut.execute(ColorInputHsvAction.SetHue(hue = 200f))
            advanceTimeBy(200.milliseconds) // the sampling period
            runCurrent()

            stateFlow.value.color shouldBe Color.Hsv(hue = 200f, saturation = 1f, value = 1f)
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. the pickers move, and the color is sampled at once
     * 3. the pickers move again, and that color waits for the next sample
     *
     * WHEN
     * a color is set from code before the next sample
     *
     * THEN
     * the waiting sample isn't applied, because the pickers no longer show it.
     */
    @Test
    fun `given a sample is pending, when a color is set from code, then the sample is NOT applied`() =
        runTest(testDispatcher) {
            val stateFlow = MutableStateFlow(MockColorInputStates.ColorInputHsvState(color = null))
            createSut(stateFlow)
            sut.execute(ColorInputHsvAction.SetHue(hue = 240f)) // sampled at once
            sut.execute(ColorInputHsvAction.SetHue(hue = 200f)) // waits for the next sample
            val colorSetFromCode = Color.Hsv(hue = 10f, saturation = 0.2f, value = 0.9f)

            stateFlow.value = MockColorInputStates.ColorInputHsvState(colorSetFromCode)
            advanceTimeBy(200.milliseconds) // the sampling period
            runCurrent()

            stateFlow.value.color shouldBe colorSetFromCode
        }

    fun createSut(
        stateFlow: MutableStateFlow<ColorInputHsvState>,
    ) =
        ColorInputHsvViewModel(
            coroutineScope = CoroutineScope(context = testDispatcher),
            atom = Atom(stateFlow),
        ).also {
            sut = it
        }
}
