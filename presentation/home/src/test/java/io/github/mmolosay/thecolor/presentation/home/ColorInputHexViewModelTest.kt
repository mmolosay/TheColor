package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputMediator
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.ViewData.TrailingIcon
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldViewModel.State
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexUiData
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorInputHexViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val viewData: ColorInputFieldUiData.ViewData = mockk(relaxed = true)

    val mediator: ColorInputMediator = mockk {
        every { hexStateFlow } returns emptyFlow()
        every { send<ColorPrototype.Hex>(any()) } just runs
    }

    lateinit var sut: ColorInputHexViewModel

    val uiData: ColorInputHexUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `filtering keeps only digits and letter A-F`() = runTest {
        createSut()

        val result = uiData.inputField.filterUserInput("123abc_!.@ABG")

        result.string shouldBe "123AB"
    }

    @Test
    fun `filtering keeps only first 6 characters`() = runTest {
        createSut()

        val result = uiData.inputField.filterUserInput("123456789ABCDEF")

        result.string shouldBe "123456"
    }

    @Test
    fun `populated state is sent to mediator on new UiData with non-empty text in input`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { viewData.trailingIcon } returns TrailingIcon.None
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            // first State.Empty is emitted on subscription due to initial text=""
            uiData.inputField.onTextChange(Text("1F")) // non-empty text

            val sentState = State.Populated(ColorPrototype.Hex("1F"))
            verify(exactly = 1) { mediator.send(sentState) }
            collectionJob.cancel()
        }

    @Test
    fun `empty state is sent to mediator on new UiData with no text in input`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { viewData.trailingIcon } returns TrailingIcon.None
            createSut()
            val collectionJob = launch {
                sut.uiDataFlow.collect() // subscriber to activate the flow
            }

            // first State.Empty is emitted on subscription due to initial text=""
            uiData.inputField.onTextChange(Text("1F")) // non-empty text
            uiData.inputField.onTextChange(Text("")) // empty text, second State.Empty

            verify(exactly = 2) { mediator.send(State.Empty) }
            collectionJob.cancel()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun createSut() =
        ColorInputHexViewModel(
            viewData = viewData,
            mediator = mediator,
            defaultDispatcher = UnconfinedTestDispatcher(),
        ).also {
            sut = it
        }
}