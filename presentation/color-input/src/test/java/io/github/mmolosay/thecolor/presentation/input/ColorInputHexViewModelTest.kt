package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexData
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.testing.MockColorInputMediatorComponents
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.github.mmolosay.thecolor.utils.invoke
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputHexViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val mediatorComponents = MockColorInputMediatorComponents()
    val mediator = mediatorComponents.mediator

    val submitAction: ColorInputSubmitAction = mockk()

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus } returns run {
            val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
            val result = PrefState.Result.HasValue(value)
            val prefState = PrefState.Ready(result)
            MutableStateFlow(prefState)
        }
    }

    val textFieldViewModelFactory: TextFieldViewModel.Factory = TextFieldViewModelTestFactory(
        userPreferencesRepository = userPreferencesRepository,
        defaultDispatcher = testDispatcher,
        uiDataUpdateDispatcher = testDispatcher,
    )

    val colorInputValidator: ColorInputValidator = mockk {
        every { any<ColorInput.Hex>().validate() } returns mockk<ColorInputValidationResult.Invalid>()
    }

    val colorInputMapper: ColorInputMapper = mockk()

    val colorConverter: ColorConverter = mockk()

    lateinit var sut: ColorInputHexViewModel

    @Test
    fun `given SUT is created, when mediator has not-null color, then text field is populated with the correct text`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorConverter) {
                every { (color as Color).toHex() } returns color
            }
            with(colorInputMapper) {
                every { color.toColorInput() } returns ColorInput.Hex("1A803F")
            }

            createSut()

            sut.data.textField.text.data shouldBe Text("1A803F")
        }

    @Test
    fun `given mediator has not-null color, when SUT is created, then the initial color is not set to mediator and update loop is not created`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x0)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Hex("").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { ColorInput.Hex("0").validate() } returns ColorInputValidationResult.Valid(color)
            }
            with(colorConverter) {
                every { (color as Color).toHex() } returns color
            }
            with(colorInputMapper) {
                every { color.toColorInput() } returns ColorInput.Hex("0")
            }

            createSut()

            coVerify(exactly = 0) {
                mediatorComponents.editor.set(color = any(), source = DomainColorInputType.Hex)
            }
        }

    @Test
    fun `when text is changed to invalid color, then 'null' color is set to mediator`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns MutableStateFlow(ColorInputMediator.InitialColorState)
            with(colorInputValidator) {
                every { ColorInput.Hex("").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { ColorInput.Hex("gibberish").validate() } returns mockk<ColorInputValidationResult.Invalid>()
            }
            createSut()

            sut.data.textField.onTextChange(Text("gibberish"))

            coVerify(exactly = 1) {
                mediatorComponents.editor.set(color = null /*invalid color input*/, source = DomainColorInputType.Hex)
            }
        }

    @Test
    fun `when mediator emits not-null color from non-HEX source, then data is updated`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
                every { ColorInput.Hex("").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { ColorInput.Hex("1A803F").validate() } returns ColorInputValidationResult.Valid(color)
            }
            with(colorConverter) {
                every { (color as Color).toHex() } returns color
            }
            with(colorInputMapper) {
                every { color.toColorInput() } returns ColorInput.Hex("1A803F")
            }
            createSut()

            run {
                val value = ColorInputMediator.ColorState(color = color, source = DomainColorInputType.Rgb, id = 1)
                colorStateFlow.emit(value)
            }

            sut.data.textField.text.data.string shouldBe "1A803F"
        }

    @Test
    fun `when mediator emits not-null color from HEX source, then data is not updated and update loop is not created`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
                every { ColorInput.Hex("").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { ColorInput.Hex("1A803F").validate() } returns ColorInputValidationResult.Valid(color)
            }
            createSut()

            run {
                val value = ColorInputMediator.ColorState(color = color, source = DomainColorInputType.Hex, id = 1)
                colorStateFlow.emit(value)
            }

            sut.data.textField.text.data.string shouldBe ""
        }

    @Test // ANCHOR:Label=0
    fun `given 'submit action' returns 'true', when invoking 'submit input', then the next data has submission result`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorAsColorInput = ColorInput.Hex("1A803F")
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Hex("").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { colorAsColorInput.validate() } returns ColorInputValidationResult.Valid(color)
            }
            with(colorConverter) {
                every { (color as Color).toHex() } returns color
            }
            with(colorInputMapper) {
                every { color.toColorInput() } returns colorAsColorInput
            }
            every { submitAction.invoke(colorInput = colorAsColorInput, validationResult = any()) } returns true
            createSut()

            sut.data.inputSubmissionResult()

            coVerify(exactly = 1) {
                submitAction.invoke(colorInput = colorAsColorInput, validationResult = any())
            }
            val ackResult = sut.data.inputSubmissionResult.result.shouldNotBeNull()
            ackResult.value.wasAccepted shouldBe true
        }

    @Test
    fun `given the data has submission result, when it is acknowledged, then the next data has it cleared`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorAsColorInput = ColorInput.Hex("1A803F")
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Hex("").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { colorAsColorInput.validate() } returns ColorInputValidationResult.Valid(color)
            }
            with(colorConverter) {
                every { (color as Color).toHex() } returns color
            }
            with(colorInputMapper) {
                every { color.toColorInput() } returns colorAsColorInput
            }
            every { submitAction.invoke(colorInput = colorAsColorInput, validationResult = any()) } returns true
            createSut()

            sut.data.inputSubmissionResult()
            val ackResult = sut.data.inputSubmissionResult.result.shouldNotBeNull() // REFERENCE:Label=0
            ackResult.ack()

            sut.data.inputSubmissionResult.result shouldBe null
        }

    @ParameterizedTest
    @MethodSource("data")
    fun `user input is filtered as expected`(
        input: String,
        expectedTextString: String,
    ) =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns MutableStateFlow(ColorInputMediator.InitialColorState)
            createSut()

            val text = sut.data.textField.filterUserInput(input)

            withClue("Filtering user input \"$input\" should return \"$expectedTextString\"") {
                text shouldBe Text(expectedTextString)
            }
        }

    fun createSut() =
        ColorInputHexViewModel(
            coroutineScope = CoroutineScope(context = testDispatcher),
            mediator = mediator,
            submitAction = submitAction,
            textFieldViewModelFactory = textFieldViewModelFactory,
            colorInputValidator = colorInputValidator,
            colorInputMapper = colorInputMapper,
            colorConverter = colorConverter,
            defaultDispatcher = testDispatcher,
            uiDataUpdateDispatcher = testDispatcher,
        ).also {
            sut = it
        }

    val ColorInputHexViewModel.data: ColorInputHexData
        get() = this.dataFlow.value

    companion object {

        @JvmStatic
        @Suppress("SpellCheckingInspection", "RedundantSuppression")
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