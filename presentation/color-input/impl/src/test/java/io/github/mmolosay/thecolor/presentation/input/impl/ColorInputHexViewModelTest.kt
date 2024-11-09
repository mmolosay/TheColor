package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEvent
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputEventStore
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputState
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexData
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.assertions.withClue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
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
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus

abstract class ColorInputHexViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val mediator: ColorInputMediator = mockk {
        every { hexColorInputFlow } returns flowOf(ColorInput.Hex(""))
        coEvery { send(color = any(), from = DomainColorInputType.Hex) } just runs
    }

    val eventStore: ColorInputEventStore = mockk()

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus() } returns kotlin.run {
            val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
            flowOf(value)
        }
    }
    val textFieldViewModelFactory: TextFieldViewModel.Factory = TextFieldViewModelTestFactory(
        userPreferencesRepository = userPreferencesRepository,
        defaultDispatcher = mainDispatcherRule.testDispatcher,
        uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
    )

    val colorInputValidator: ColorInputValidator = mockk {
        every { any<ColorInput>().validate() } returns mockk<ColorInputState.Invalid>()
    }

    lateinit var sut: ColorInputHexViewModel

    fun createSut() =
        ColorInputHexViewModel(
            coroutineScope = TestScope(context = mainDispatcherRule.testDispatcher),
            mediator = mediator,
            eventStore = eventStore,
            textFieldViewModelFactory = textFieldViewModelFactory,
            colorInputValidator = colorInputValidator,
            defaultDispatcher = mainDispatcherRule.testDispatcher,
            uiDataUpdateDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    val dataState: DataState<ColorInputHexData>
        get() = sut.dataStateFlow.value

    val data: ColorInputHexData
        get() = dataState.shouldBeInstanceOf<DataState.Ready<ColorInputHexData>>().data
}

@RunWith(Parameterized::class)
class FilterHexUserInputTest(
    val string: String,
    val expectedTextString: String,
) : ColorInputHexViewModelTest() {

    @Test
    fun `user input is filtered as expected`() {
        createSut()

        val text = data.textField.filterUserInput(string)

        withClue("Filtering user input \"$string\" should return $expectedTextString") {
            text shouldBe Text(expectedTextString)
        }
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            // can't work with Text() directly because it's a value class and inlined in runtime
            /* #0  */ "" shouldBeFilteredTo "",
            /* #1  */ "0" shouldBeFilteredTo "0",
            /* #2  */ "E" shouldBeFilteredTo "E",
            /* #3  */ "30" shouldBeFilteredTo "30",
            /* #4  */ "1A803F" shouldBeFilteredTo "1A803F",
            /* #5  */ "123abc_!.@ABG" shouldBeFilteredTo "123ABC",
            /* #6  */ "x!1y_2z^3ABC" shouldBeFilteredTo "123ABC",
            /* #7  */ "1234567890" shouldBeFilteredTo "123456",
            /* #8  */ "123456789ABCDEF" shouldBeFilteredTo "123456",
        )

        infix fun String.shouldBeFilteredTo(expectedText: String): Array<Any> =
            arrayOf(this, expectedText)
    }
}

class OtherHex : ColorInputHexViewModelTest() {

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

    @Test
    fun `initial data is not sent to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            coVerify(exactly = 0) {
                mediator.send(color = any(), from = DomainColorInputType.Hex)
            }
            collectionJob.cancel()
        }

    @Test
    fun `changing input text to invalid color sends 'null' to mediator`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val colorInput = ColorInput.Hex("1F")
            every {
                with(colorInputValidator) { colorInput.validate() }
            } returns mockk<ColorInputState.Invalid>()
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            data.textField.onTextChange(Text("1F"))

            coVerify(exactly = 1) {
                mediator.send(
                    color = null, // invalid color input
                    from = DomainColorInputType.Hex,
                )
            }
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator updates data`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val hexColorInputFlow = MutableSharedFlow<ColorInput.Hex>()
            every { mediator.hexColorInputFlow } returns hexColorInputFlow
            every {
                with(colorInputValidator) { ColorInput.Hex("1F").validate() }
            } returns mockk<ColorInputState.Invalid>()
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
            every {
                with(colorInputValidator) { ColorInput.Hex("1F").validate() }
            } returns mockk<ColorInputState.Invalid>()
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            val sentColorInput = ColorInput.Hex("1F")
            hexColorInputFlow.emit(sentColorInput)

            coVerify(exactly = 0) {
                mediator.send(color = any(), from = DomainColorInputType.Hex)
            }
            collectionJob.cancel()
        }

    @Test
    fun `invoking 'submit color' sends 'Submit' event`() {
        coEvery { eventStore.send(event = any()) } just runs
        createSut()

        data.submitColor()

        coVerify(exactly = 1) {
            eventStore.send(event = any<ColorInputEvent.Submit>())
        }
    }
}