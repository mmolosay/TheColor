package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator.InputType
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.rgb.ColorInputRgbData
import io.github.mmolosay.thecolor.presentation.input.impl.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.assertions.withClue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

abstract class ColorInputRgbViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val mediator: ColorInputMediator = mockk {
        every { rgbColorInputFlow } returns flowOf(ColorInput.Rgb("", "", ""))
        coEvery { send(color = any(), from = InputType.Rgb) } just runs
    }
    val eventStore: ColorInputEventStore = mockk()
    val colorInputValidator: ColorInputValidator = mockk {
        every { any<ColorInput>().validate() } returns mockk<ColorInputState.Invalid>()
    }

    lateinit var sut: ColorInputRgbViewModel

    fun createSut() =
        ColorInputRgbViewModel(
            coroutineScope = TestScope(mainDispatcherRule.testDispatcher),
            mediator = mediator,
            eventStore = eventStore,
            colorInputValidator = colorInputValidator,
            uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    val dataState: DataState<ColorInputRgbData>
        get() = sut.dataStateFlow.value

    val data: ColorInputRgbData
        get() {
            dataState should beOfType<DataState.Ready<*>>() // assertion for clear failure message
            return (dataState as DataState.Ready).data
        }
}

@RunWith(Parameterized::class)
class FilterUserInputTest(
    val string: String,
    val expectedTextString: String,
) : ColorInputRgbViewModelTest() {

    @Test
    fun `user input is filtered as expected`() {
        createSut()

        val text = data.rTextField.filterUserInput(string)

        withClue("Filtering user input \"$string\" should return $expectedTextString") {
            text shouldBe Text(expectedTextString)
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            // can't work with Text() directly because it's a value class and inlined in runtime
            /* #0  */ "abc1def2ghi3" shouldBeFilteredTo "123",
            /* #1  */ "1234567890" shouldBeFilteredTo "123",
            /* #2  */ "" shouldBeFilteredTo "",
            /* #3  */ "0" shouldBeFilteredTo "0",
            /* #4  */ "03" shouldBeFilteredTo "3",
            /* #5  */ "003" shouldBeFilteredTo "3",
            /* #6  */ "000" shouldBeFilteredTo "0",
            /* #7  */ "30" shouldBeFilteredTo "30",
            /* #8  */ "255" shouldBeFilteredTo "255",
            /* #9  */ "256" shouldBeFilteredTo "25",
        )

        infix fun String.shouldBeFilteredTo(expectedText: String): Array<Any> =
            arrayOf(this, expectedText)
    }
}

class Other : ColorInputRgbViewModelTest() {

    @Test
    fun `sut is created with state BeingInitialized if mediator RGB flow has no value yet`() {
        every { mediator.rgbColorInputFlow } returns emptyFlow()

        createSut()

        dataState should beOfType<DataState.BeingInitialized>()
    }

    @Test
    fun `sut is created with state Ready if mediator RGB flow has value already`() {
        createSut()

        dataState should beOfType<DataState.Ready<*>>()
    }

    @Test
    fun `state becomes Ready when mediator emits first value from RGB flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val rgbColorInputFlow = MutableSharedFlow<ColorInput.Rgb>()
            every { mediator.rgbColorInputFlow } returns rgbColorInputFlow
            createSut()

            rgbColorInputFlow.emit(ColorInput.Rgb("18", "1", "20"))

            dataState should beOfType<DataState.Ready<*>>()
        }

    @Test
    fun `initial data is not sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            coVerify(exactly = 0) { mediator.send(color = any(), from = InputType.Rgb) }
            collectionJob.cancel()
        }

    @Test
    fun `data updated from UI is sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val parsedColor = mockk<Color>()
            every {
                with(colorInputValidator) { ColorInput.Rgb("18", "", "").validate() }
            } returns mockk<ColorInputState.Invalid>()
            every {
                with(colorInputValidator) { ColorInput.Rgb("18", "1", "").validate() }
            } returns mockk<ColorInputState.Invalid>()
            every {
                with(colorInputValidator) { ColorInput.Rgb("18", "1", "20").validate() }
            } returns ColorInputState.Valid(parsedColor)
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            data.rTextField.onTextChange(Text("18"))
            data.gTextField.onTextChange(Text("1"))
            data.bTextField.onTextChange(Text("20"))

            coVerify(exactly = 1) {
                mediator.send(color = parsedColor, from = InputType.Rgb)
            }
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator updates data`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val rgbColorInputFlow = MutableSharedFlow<ColorInput.Rgb>()
            every { mediator.rgbColorInputFlow } returns rgbColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            ColorInput.Rgb(r = "18", g = "1", b = "20")
                .also { rgbColorInputFlow.emit(it) }

            data.rTextField.text.string shouldBe "18"
            data.gTextField.text.string shouldBe "1"
            data.bTextField.text.string shouldBe "20"
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator is not sent back to mediator and emission loop is not created`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val rgbColorInputFlow = MutableSharedFlow<ColorInput.Rgb>()
            every { mediator.rgbColorInputFlow } returns rgbColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            val sentColorInput = ColorInput.Rgb(r = "18", g = "1", b = "20")
            rgbColorInputFlow.emit(sentColorInput)

            coVerify(exactly = 0) {
                mediator.send(color = any(), from = InputType.Rgb)
            }
            collectionJob.cancel()
        }

    @Test
    fun `invoking 'submit color' sends 'Submit' event`() {
        coEvery { eventStore.send(event = any()) } just runs
        createSut()

        data.submitColor()

        coVerify(exactly = 1) {
            eventStore.send(event = ColorInputEvent.Submit)
        }
    }
}