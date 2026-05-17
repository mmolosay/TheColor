package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbData
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.input.testing.MockColorInputMediatorComponents
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.github.mmolosay.thecolor.utils.pending
import io.kotest.assertions.withClue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.kotest.matchers.types.shouldBeInstanceOf
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
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace as DomainSmartBackspace

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputRgbViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val mediatorComponents = MockColorInputMediatorComponents()
    val mediator = mediatorComponents.mediator

    val submitAction: ColorInputSubmitAction = mockk()

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus } returns run {
            val value = SelectAllTextOnTextFieldFocus(enabled = false)
            val dataState = UserPreferencesRepository.DataState.HasValueStored(value)
            MutableStateFlow(dataState)
        }
        every { flowOfSmartBackspace } returns run {
            val value = DomainSmartBackspace(enabled = false)
            val dataState = UserPreferencesRepository.DataState.HasValueStored(value)
            MutableStateFlow(dataState)
        }
    }

    val textFieldViewModelFactory: TextFieldViewModel.Factory = TextFieldViewModelTestFactory(
        userPreferencesRepository = userPreferencesRepository,
        defaultDispatcher = testDispatcher,
        uiDataUpdateDispatcher = testDispatcher,
    )

    val colorInputValidator: ColorInputValidator = mockk {
        every { any<ColorInput>().validate() } returns mockk<ColorInputValidationResult.Invalid>()
    }

    val colorInputMapper: ColorInputMapper = mockk()

    val colorConverter: ColorConverter = mockk()

    lateinit var sut: ColorInputRgbViewModel

    @Test // ANCHOR:Label=0
    fun `given SUT is created, when mediator has not-null color, then data state becomes Ready`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x0)
            val colorInRgb = Color.Rgb(0, 0, 0)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorConverter) {
                every { (color as Color).toRgb() } returns colorInRgb
            }
            with(colorInputMapper) {
                every { colorInRgb.toColorInput() } returns ColorInput.Rgb("0", "0", "0")
            }

            createSut()

            dataState should beOfType<DataState.Ready<*>>()
        }

    @Test
    fun `given SUT is created, when mediator has not-null color, then text fields are populated with the correct text`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorInRgb = Color.Rgb(26, 128, 63)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorConverter) {
                every { (color as Color).toRgb() } returns colorInRgb
            }
            with(colorInputMapper) {
                every { colorInRgb.toColorInput() } returns ColorInput.Rgb("26", "128", "63")
            }

            createSut()

            // REFERENCE:Label=0
            val data = dataState.shouldBeInstanceOf<DataState.Ready<ColorInputRgbData>>().data
            data.rTextField.text.data shouldBe Text("26")
            data.gTextField.text.data shouldBe Text("128")
            data.bTextField.text.data shouldBe Text("63")
        }

    @Test
    fun `given SUT is created, when mediator has 'null' color, then data state becomes Ready nonetheless`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 0)
                MutableStateFlow(value)
            }

            createSut()

            dataState should beOfType<DataState.Ready<*>>()
        }

    @Test
    fun `given mediator has not-null color, when SUT is created, then the initial color is not set to mediator and update loop is not created`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x0)
            val colorInRgb = Color.Rgb(0, 0, 0)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Rgb("", "", "").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { ColorInput.Rgb("0", "0", "0").validate() } returns ColorInputValidationResult.Valid(colorInRgb)
            }
            with(colorConverter) {
                every { (color as Color).toRgb() } returns colorInRgb
            }
            with(colorInputMapper) {
                every { colorInRgb.toColorInput() } returns ColorInput.Rgb("0", "0", "0")
            }

            createSut()

            coVerify(exactly = 0) {
                mediatorComponents.editor.set(color = any(), source = DomainColorInputType.Rgb)
            }
        }

    @Test
    fun `when text is changed to invalid color, then 'null' color is set to mediator`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = null, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Rgb("", "", "").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { ColorInput.Rgb("gib", "ber", "rish").validate() } returns mockk<ColorInputValidationResult.Invalid>()
            }
            createSut()

            data.rTextField.onTextChange(Text("gib"))
            data.gTextField.onTextChange(Text("ber"))
            data.bTextField.onTextChange(Text("rish"))

            // one call to 'mediator.set()' for each update in 3 text fields, 3 total
            coVerify(exactly = 3) {
                mediatorComponents.editor.set(color = null /*invalid color input*/, source = DomainColorInputType.Rgb)
            }
        }

    @Test
    fun `when mediator emits not-null color from non-RGB source, then data is updated`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorInRgb = Color.Rgb(26, 128, 63)
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
                every { ColorInput.Rgb("", "", "").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { ColorInput.Rgb("26", "128", "63").validate() } returns ColorInputValidationResult.Valid(color)
            }
            with(colorConverter) {
                every { (color as Color).toRgb() } returns colorInRgb
            }
            with(colorInputMapper) {
                every { colorInRgb.toColorInput() } returns ColorInput.Rgb("26", "128", "63")
            }
            createSut()

            run {
                val value = ColorInputMediator.ColorState(
                    color = color,
                    source = DomainColorInputType.Hex,
                    id = 1,
                )
                colorStateFlow.emit(value)
            }

            data.rTextField.text.data.string shouldBe "26"
            data.gTextField.text.data.string shouldBe "128"
            data.bTextField.text.data.string shouldBe "63"
        }

    @Test
    fun `when mediator emits not-null color from RGB source, then data is not updated and update loop is not created`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
                every { ColorInput.Rgb("", "", "").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { ColorInput.Rgb("26", "128", "63").validate() } returns ColorInputValidationResult.Valid(color)
            }
            createSut()

            run {
                val value = ColorInputMediator.ColorState(
                    color = color,
                    source = DomainColorInputType.Rgb,
                    id = 1,
                )
                colorStateFlow.emit(value)
            }

            data.rTextField.text.data.string shouldBe ""
            data.gTextField.text.data.string shouldBe ""
            data.bTextField.text.data.string shouldBe ""
        }

    @Test
    fun `given 'submit action' returns 'true', when invoking 'submit input', then 'submission result' is emitted`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorInRgb = Color.Rgb(26, 128, 63)
            val colorAsColorInput = ColorInput.Rgb("26", "128", "63")
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Rgb("", "", "").validate() } returns mockk<ColorInputValidationResult.Invalid>()
                every { colorAsColorInput.validate() } returns ColorInputValidationResult.Valid(color)
            }
            with(colorConverter) {
                every { (color as Color).toRgb() } returns colorInRgb
            }
            with(colorInputMapper) {
                every { colorInRgb.toColorInput() } returns colorAsColorInput
            }
            every { submitAction.invoke(colorInput = colorAsColorInput, validationResult = any()) } returns true
            createSut()

            data.submitInput()

            coVerify(exactly = 1) {
                submitAction.invoke(colorInput = colorAsColorInput, validationResult = any())
            }
            val submissionResult = sut.submissionResultStore.pending.last().value
            submissionResult.wasAccepted shouldBe true
        }

    @Test
    fun `given SUT is created, when 'smart backspace' feature value is 'being initialized', then data state becomes Ready nonetheless`() =
        runTest(testDispatcher) {
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
                every { ColorInput.Rgb("", "", "").validate() } returns mockk<ColorInputValidationResult.Invalid>()
            }
            every { userPreferencesRepository.flowOfSmartBackspace } returns run {
                val dataState = UserPreferencesRepository.DataState.BeingInitialized
                MutableStateFlow(dataState)
            }

            createSut()

            dataState should beOfType<DataState.Ready<*>>()
        }

    @Test
    fun `given SUT is created, when 'smart backspace' feature value is 'being initialized', then data has default value for 'is smart backspace enabled'`() =
        runTest(testDispatcher) {
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
                every { ColorInput.Rgb("", "", "").validate() } returns mockk<ColorInputValidationResult.Invalid>()
            }
            every { userPreferencesRepository.flowOfSmartBackspace } returns run {
                val dataState = UserPreferencesRepository.DataState.BeingInitialized
                MutableStateFlow(dataState)
            }

            createSut()

            data.isSmartBackspaceEnabled shouldBe DefaultUserPreferences.SmartBackspace.enabled
        }

    @Test
    fun `when 'smart backspace' feature value changes, then data is updated accordingly`() =
        runTest(testDispatcher) {
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
                every { ColorInput.Rgb("", "", "").validate() } returns mockk<ColorInputValidationResult.Invalid>()
            }
            val flowOfSmartBackspace = run {
                val dataState = UserPreferencesRepository.DataState.BeingInitialized
                MutableStateFlow<UserPreferencesRepository.DataState<DomainSmartBackspace>>(dataState)
            }
            every { userPreferencesRepository.flowOfSmartBackspace } returns flowOfSmartBackspace

            createSut()

            // WHEN-THEN #1
            run {
                val value = DomainSmartBackspace(enabled = false)
                val dataState = UserPreferencesRepository.DataState.HasValueStored(value)
                flowOfSmartBackspace.emit(dataState)
                data.isSmartBackspaceEnabled shouldBe false
            }

            // WHEN-THEN #2
            run {
                val value = DomainSmartBackspace(enabled = true)
                val dataState = UserPreferencesRepository.DataState.HasValueStored(value)
                flowOfSmartBackspace.emit(dataState)
                data.isSmartBackspaceEnabled shouldBe true
            }
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

            // we check only one component because the logic is same for all 3 of them
            val text = data.rTextField.filterUserInput(input)

            withClue("Filtering user input \"$input\" should return \"$expectedTextString\"") {
                text shouldBe Text(expectedTextString)
            }
        }

    fun createSut() =
        ColorInputRgbViewModel(
            coroutineScope = CoroutineScope(testDispatcher),
            mediator = mediator,
            submitAction = submitAction,
            textFieldViewModelFactory = textFieldViewModelFactory,
            colorInputValidator = colorInputValidator,
            colorInputMapper = colorInputMapper,
            colorConverter = colorConverter,
            userPreferencesRepository = userPreferencesRepository,
            uiDataUpdateDispatcher = testDispatcher,
            defaultDispatcher = testDispatcher,
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

    companion object {

        @JvmStatic
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