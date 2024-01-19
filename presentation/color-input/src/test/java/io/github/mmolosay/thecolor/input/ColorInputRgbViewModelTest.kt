package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.input.field.ColorInputFieldUiData.Text
import io.github.mmolosay.thecolor.input.field.ColorInputFieldUiData.ViewData.TrailingIcon
import io.github.mmolosay.thecolor.input.field.ColorInputFieldViewModel.State
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbUiData
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbViewData
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorInputRgbViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val rInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }
    val gInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }
    val bInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true) {
        every { trailingIcon } returns TrailingIcon.None
    }
    val viewData = ColorInputRgbViewData(rInputField, gInputField, bInputField)

    val initialTextProvider: InitialTextProvider = mockk(relaxed = true) {
        every { rgbR } returns Text("mocked")
        every { rgbG } returns Text("mocked")
        every { rgbB } returns Text("mocked")
    }

    val mediator: ColorInputMediator = mockk {
        every { rgbStateFlow } returns emptyFlow()
        every { send<ColorPrototype.Hex>(any()) } just runs
    }

    lateinit var sut: ColorInputRgbViewModel

    val uiData: ColorInputRgbUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `filtering keeps only digits`() {
        createSut()

        val result = uiData.rInputField.filterUserInput("abc1def2ghi3")

        result.string shouldBe "123"
    }

    @Test
    fun `filtering keeps only first 3 characters`() {
        createSut()

        val result = uiData.rInputField.filterUserInput("1234567890")

        result.string shouldBe "123"
    }

    @Test
    fun `populated state is sent to mediator on initial UiData with non-empty text in inputs`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { initialTextProvider.rgbR } returns Text("18")
            every { initialTextProvider.rgbG } returns Text("")
            every { initialTextProvider.rgbB } returns Text("")

            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            val sentState = State.Populated(ColorPrototype.Rgb(r = 18, g = null, b = null))
            verify(exactly = 1) { mediator.send(sentState) }
            collectionJob.cancel()
        }

    @Test
    fun `empty state is sent to mediator on initial UiData with empty text in inputs`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { initialTextProvider.rgbR } returns Text("")
            every { initialTextProvider.rgbG } returns Text("")
            every { initialTextProvider.rgbB } returns Text("")

            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            verify(exactly = 1) { mediator.send(State.Empty) }
            collectionJob.cancel()
        }

    @Test
    fun `populated state is sent to mediator on new UiData with non-empty text in input`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            uiData.rInputField.onTextChange(Text("18"))

            val sentState = State.Populated(ColorPrototype.Rgb(r = 18, g = null, b = null))
            verify(exactly = 1) { mediator.send(sentState) }
            collectionJob.cancel()
        }

    @Test
    fun `empty state is sent to mediator on new UiData with no text in inputs`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { initialTextProvider.rgbR } returns Text("18")
            every { initialTextProvider.rgbG } returns Text("")
            every { initialTextProvider.rgbB } returns Text("")

            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            uiData.rInputField.onTextChange(Text(""))

            verify(exactly = 1) { mediator.send(State.Empty) }
            collectionJob.cancel()
        }

    @Test
    fun `empty state from mediator clears inputs text`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initialState = State.Populated(ColorPrototype.Rgb(r = 18, g = null, b = null))
            val rgbStateFlow = MutableStateFlow<State<ColorPrototype.Rgb>>(initialState)
            every { mediator.rgbStateFlow } returns rgbStateFlow
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            // initial text is not empty
            rgbStateFlow.value = State.Empty

            uiData.rInputField.text.string shouldBe ""
            collectionJob.cancel()
        }

    @Test
    fun `populated state from mediator updates input text`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val initialState = State.Empty
            val rgbStateFlow = MutableStateFlow<State<ColorPrototype.Rgb>>(initialState)
            every { mediator.rgbStateFlow } returns rgbStateFlow
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            // initial text is empty
            rgbStateFlow.value = State.Populated(ColorPrototype.Rgb(r = 18, g = 0, b = 0))

            uiData.rInputField.text.string shouldBe "18"
            collectionJob.cancel()
        }

    fun createSut() =
        ColorInputRgbViewModel(
            viewData = viewData,
            initialTextProvider = initialTextProvider,
            mediator = mediator,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }
}