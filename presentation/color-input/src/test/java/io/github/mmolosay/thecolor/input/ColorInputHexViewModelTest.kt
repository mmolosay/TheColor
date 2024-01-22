package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.Text
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData.TrailingIcon
import io.github.mmolosay.thecolor.input.hex.ColorInputHexUiData
import io.github.mmolosay.thecolor.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.input.model.ColorInput
import io.github.mmolosay.thecolor.input.model.UiState
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

class ColorInputHexViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val viewData: TextFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }

    val mediator: ColorInputMediator = mockk {
        every { hexColorInputFlow } returns flowOf(ColorInput.Hex(""))
        coEvery { send(any()) } just runs
    }

    lateinit var sut: ColorInputHexViewModel

    val uiState: UiState<ColorInputHexUiData>
        get() = sut.uiStateFlow.value

    val uiData: ColorInputHexUiData
        get() {
            uiState should beOfType<UiState.Ready<*>>() // assertion for clear failure message
            return (uiState as UiState.Ready).uiData
        }

    @Test
    fun `sut is created with UiState BeingInitialized if mediator HEX flow has no value yet`() {
        every { mediator.hexColorInputFlow } returns emptyFlow()

        createSut()

        uiState should beOfType<UiState.BeingInitialized>()
    }

    @Test
    fun `sut is created with UiState Ready if mediator HEX flow has value already`() {
        createSut()

        uiState should beOfType<UiState.Ready<*>>()
    }

    @Test
    fun `UiState becomes Ready when mediator emits first value from HEX flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val hexColorInputFlow = MutableSharedFlow<ColorInput.Hex>()
            every { mediator.hexColorInputFlow } returns hexColorInputFlow
            createSut()

            hexColorInputFlow.emit(ColorInput.Hex(""))

            uiState should beOfType<UiState.Ready<*>>()
        }

    @Test
    fun `filtering keeps only digits and letters A-F`() {
        createSut()

        val result = uiData.textField.filterUserInput("123abc_!.@ABG")

        result.string shouldBe "123AB"
    }

    @Test
    fun `filtering keeps only first 6 characters`() {
        createSut()

        val result = uiData.textField.filterUserInput("123456789ABCDEF")

        result.string shouldBe "123456"
    }

    @Test
    fun `initial UiData is not sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.uiStateFlow.collect() // subscriber to activate the flow
            }

            coVerify(exactly = 0) { mediator.send(any()) }
            collectionJob.cancel()
        }

    @Test
    fun `UiData updated from UI is sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.uiStateFlow.collect() // subscriber to activate the flow
            }

            uiData.textField.onTextChange(Text("1F"))

            val sentColorInput = ColorInput.Hex("1F")
            coVerify(exactly = 1) { mediator.send(sentColorInput) }
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator updates UiData`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val hexColorInputFlow = MutableSharedFlow<ColorInput.Hex>()
            every { mediator.hexColorInputFlow } returns hexColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.uiStateFlow.collect() // subscriber to activate the flow
            }

            hexColorInputFlow.emit(ColorInput.Hex("1F"))

            uiData.textField.text.string shouldBe "1F"
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator is not sent back to mediator and emission loop is not created`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val hexColorInputFlow = MutableSharedFlow<ColorInput.Hex>()
            every { mediator.hexColorInputFlow } returns hexColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.uiStateFlow.collect() // subscriber to activate the flow
            }

            val sentColorInput = ColorInput.Hex("1F")
            hexColorInputFlow.emit(sentColorInput)

            coVerify(exactly = 0) { mediator.send(sentColorInput) }
            collectionJob.cancel()
        }

    fun createSut() =
        ColorInputHexViewModel(
            viewData = viewData,
            mediator = mediator,
            uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}