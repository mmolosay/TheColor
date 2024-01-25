package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.input.field.TextFieldData.Text
import io.github.mmolosay.thecolor.input.model.ColorInput
import io.github.mmolosay.thecolor.input.model.DataState
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbData
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
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
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorInputRgbViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val mediator: ColorInputMediator = mockk {
        every { rgbColorInputFlow } returns flowOf(ColorInput.Rgb("", "", ""))
        coEvery { send(any()) } just runs
    }

    lateinit var sut: ColorInputRgbViewModel

    val dataState: DataState<ColorInputRgbData>
        get() = sut.dataStateFlow.value

    val uiData: ColorInputRgbData
        get() {
            dataState should beOfType<DataState.Ready<*>>() // assertion for clear failure message
            return (dataState as DataState.Ready).data
        }

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
    fun `initial data is not sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            coVerify(exactly = 0) { mediator.send(any()) }
            collectionJob.cancel()
        }

    @Test
    fun `data updated from UI is sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            uiData.rTextField.onTextChange(Text("18"))
            uiData.gTextField.onTextChange(Text("1"))
            uiData.bTextField.onTextChange(Text("20"))

            val sentColorInput = ColorInput.Rgb(r = "18", g = "1", b = "20")
            coVerify(exactly = 1) { mediator.send(sentColorInput) }
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

            uiData.rTextField.text.string shouldBe "18"
            uiData.gTextField.text.string shouldBe "1"
            uiData.bTextField.text.string shouldBe "20"
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

            coVerify(exactly = 0) { mediator.send(sentColorInput) }
            collectionJob.cancel()
        }

    fun createSut() =
        ColorInputRgbViewModel(
            mediator = mediator,
            uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}