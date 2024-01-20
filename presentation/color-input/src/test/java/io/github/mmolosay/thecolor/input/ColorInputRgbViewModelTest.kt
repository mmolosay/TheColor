package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData.TrailingIcon
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbUiData
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbViewData
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorInputRgbViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val rInputField: TextFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }
    val gInputField: TextFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }
    val bInputField: TextFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }
    val viewData = ColorInputRgbViewData(rInputField, gInputField, bInputField)

    val initialTextProvider: InitialTextProvider = mockk(relaxed = true) {
        every { rgbR } returns Text("mocked")
        every { rgbG } returns Text("mocked")
        every { rgbB } returns Text("mocked")
    }

    val mediator: ColorInputMediator = mockk {
        every { rgbColorInputFlow } returns emptyFlow()
        every { send(any()) } just runs
    }

    lateinit var sut: ColorInputRgbViewModel

    val uiData: ColorInputRgbUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `filtering keeps only digits`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("abc1def2ghi3")

        result.string shouldBe "123"
    }

    @Test
    fun `filtering keeps only first 3 characters`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("1234567890")

        result.string shouldBe "123"
    }

    @Test
    fun `filtering of empty string returns empty string`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("")

        result.string shouldBe ""
    }

    @Test
    fun `filtering of 0 returns 0`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("0")

        result.string shouldBe "0"
    }

    @Test
    fun `filtering of 003 returns 3`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("003")

        result.string shouldBe "3"
    }

    @Test
    fun `filtering of 000 returns 0`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("000")

        result.string shouldBe "0"
    }

    @Test
    fun `filtering of 30 returns 30`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("30")

        result.string shouldBe "30"
    }

    @Test
    fun `filtering of 255 returns 255`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("255")

        result.string shouldBe "255"
    }

    @Test
    fun `filtering of 256 returns 25`() {
        createSut()

        val result = uiData.rTextField.filterUserInput("256")

        result.string shouldBe "25"
    }

    @Test
    fun `initial UiData is not sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            verify(exactly = 0) { mediator.send(any()) }
            collectionJob.cancel()
        }

    @Test
    fun `updated UiData is sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { initialTextProvider.rgbR } returns Text("")
            every { initialTextProvider.rgbG } returns Text("")
            every { initialTextProvider.rgbB } returns Text("")
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            uiData.rTextField.onTextChange(Text("18"))

            val sentColorInput = ColorInput.Rgb(r = "18", g = "", b = "")
            verify(exactly = 1) { mediator.send(sentColorInput) }
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator updates UiData`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val rgbColorInputFlow = MutableSharedFlow<ColorInput.Rgb>()
            every { mediator.rgbColorInputFlow } returns rgbColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            ColorInput.Rgb(r = "18", g = "1", b = "20")
                .also { rgbColorInputFlow.emit(it) }

            uiData.rTextField.text.string shouldBe "18"
            uiData.gTextField.text.string shouldBe "1"
            uiData.bTextField.text.string shouldBe "20"
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator is not emitted to mediator back and loop is not created`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val rgbColorInputFlow = MutableSharedFlow<ColorInput.Rgb>()
            every { mediator.rgbColorInputFlow } returns rgbColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            val sentColorInput = ColorInput.Rgb(r = "18", g = "1", b = "20")
            rgbColorInputFlow.emit(sentColorInput)

            verify(exactly = 0) { mediator.send(sentColorInput) }
            collectionJob.cancel()
        }

    fun createSut() =
        ColorInputRgbViewModel(
            viewData = viewData,
            initialTextProvider = initialTextProvider,
            mediator = mediator,
            mediatorUpdatesCollectionDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}