package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.presentation.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexData
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.DataState
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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorInputHexViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val mediator: ColorInputMediator = mockk {
        every { hexColorInputFlow } returns flowOf(ColorInput.Hex(""))
        coEvery { send(any()) } just runs
    }
    val eventStore: ColorInputEventStore = mockk()

    lateinit var sut: ColorInputHexViewModel

    val dataState: DataState<ColorInputHexData>
        get() = sut.dataStateFlow.value

    val data: ColorInputHexData
        get() {
            dataState should beOfType<DataState.Ready<*>>() // assertion for clear failure message
            return (dataState as DataState.Ready).data
        }

    @Test
    fun `SUT is created with state BeingInitialized if mediator HEX flow has no value yet`() {
        every { mediator.hexColorInputFlow } returns emptyFlow()

        createSut()

        dataState should beOfType<DataState.BeingInitialized>()
    }

    @Test
    fun `SUT is created with state Ready if mediator HEX flow has value already`() {
        every { mediator.hexColorInputFlow } returns flowOf(ColorInput.Hex(""))

        createSut()

        dataState should beOfType<DataState.Ready<*>>()
    }

    @Test
    fun `state becomes Ready when mediator emits first value from HEX flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val hexColorInputFlow = MutableSharedFlow<ColorInput.Hex>()
            every { mediator.hexColorInputFlow } returns hexColorInputFlow
            createSut()

            hexColorInputFlow.emit(ColorInput.Hex(""))

            dataState should beOfType<DataState.Ready<*>>()
        }

    // TODO: make tests for filtering parameterized and add more test cases

    @Test
    fun `filtering keeps only digits and letters A-F`() {
        createSut()

        val result = data.textField.filterUserInput("123abc_!.@ABG")

        result.string shouldBe "123ABC"
    }

    @Test
    fun `filtering keeps only first 6 characters`() {
        createSut()

        val result = data.textField.filterUserInput("123456789ABCDEF")

        result.string shouldBe "123456"
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

            data.textField.onTextChange(Text("1F"))

            val sentColorInput = ColorInput.Hex("1F")
            coVerify(exactly = 1) { mediator.send(sentColorInput) }
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator updates data`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val hexColorInputFlow = MutableSharedFlow<ColorInput.Hex>()
            every { mediator.hexColorInputFlow } returns hexColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            hexColorInputFlow.emit(ColorInput.Hex("1F"))

            data.textField.text.string shouldBe "1F"
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator is not sent back to mediator and emission loop is not created`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val hexColorInputFlow = MutableSharedFlow<ColorInput.Hex>()
            every { mediator.hexColorInputFlow } returns hexColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            val sentColorInput = ColorInput.Hex("1F")
            hexColorInputFlow.emit(sentColorInput)

            coVerify(exactly = 0) { mediator.send(sentColorInput) }
            collectionJob.cancel()
        }

    @Test
    fun `invoking 'submit color' sends 'Submit' event`() {
        coEvery { eventStore.send(event = any()) } just runs
        createSut()

        data.submitColor()

        coVerify(exactly = 1) { eventStore.send(event = ColorInputEvent.Submit) }
    }

    fun createSut() =
        ColorInputHexViewModel(
            coroutineScope = TestScope(context = mainDispatcherRule.testDispatcher),
            mediator = mediator,
            eventStore = eventStore,
            uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}