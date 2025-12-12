package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.repository.UserPreferencesRepository
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbData
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewModel
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.SmartBackspace as DomainSmartBackspace

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputRgbViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val mediator: ColorInputMediator = mockk {
        every { rgbColorInputFlow } returns flowOf(ColorInput.Rgb("", "", ""))
        coEvery { send(color = any(), from = DomainColorInputType.Rgb) } just runs
    }

    val submitAction: ColorInputSubmitAction = mockk()

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus } returns run {
            val value = SelectAllTextOnTextFieldFocus(enabled = false)
            MutableStateFlow(value)
        }
        every { flowOfSmartBackspace } returns run {
            val value = DomainSmartBackspace(enabled = false)
            MutableStateFlow(value)
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

    lateinit var sut: ColorInputRgbViewModel

    @Test
    fun `state becomes Ready short after SUT is created even if mediator RGB flow has no value yet`() {
        every { mediator.rgbColorInputFlow } returns emptyFlow()

        createSut()

        dataState should beOfType<DataState.Ready<*>>()
    }

    @Test
    fun `state becomes Ready short after SUT is created even if mediator RGB flow has value already`() {
        every { mediator.rgbColorInputFlow } returns flowOf(ColorInput.Rgb("", "", ""))

        createSut()

        dataState should beOfType<DataState.Ready<*>>()
    }

    @Test
    fun `state becomes 'Ready' when mediator emits first value from RGB flow`() =
        runTest(testDispatcher) {
            val rgbColorInputFlow = MutableSharedFlow<ColorInput.Rgb>()
            every { mediator.rgbColorInputFlow } returns rgbColorInputFlow
            createSut()

            rgbColorInputFlow.emit(ColorInput.Rgb("18", "1", "20"))

            dataState should beOfType<DataState.Ready<*>>()
        }

    @Test
    fun `state becomes Ready when 'null' initial value from 'smart backspace' is emitted`() =
        runTest(testDispatcher) {
            val flowOfSmartBackspace = MutableStateFlow<DomainSmartBackspace?>(null)
            every { userPreferencesRepository.flowOfSmartBackspace } returns flowOfSmartBackspace

            createSut()

            dataState should beOfType<DataState.Ready<*>>()
        }

    @Test
    fun `initial data is not sent to mediator`() =
        runTest(testDispatcher) {
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            coVerify(exactly = 0) { mediator.send(color = any(), from = DomainColorInputType.Rgb) }
            collectionJob.cancel()
        }

    @Test
    fun `data updated from UI is sent to mediator`() =
        runTest(testDispatcher) {
            val parsedColor = mockk<Color>()
            every {
                with(colorInputValidator) { ColorInput.Rgb("18", "", "").validate() }
            } returns mockk<ColorInputValidationResult.Invalid>()
            every {
                with(colorInputValidator) { ColorInput.Rgb("18", "1", "").validate() }
            } returns mockk<ColorInputValidationResult.Invalid>()
            every {
                with(colorInputValidator) { ColorInput.Rgb("18", "1", "20").validate() }
            } returns ColorInputValidationResult.Valid(parsedColor)
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            data.rTextField.onTextChange(Text("18"))
            data.gTextField.onTextChange(Text("1"))
            data.bTextField.onTextChange(Text("20"))

            coVerify(exactly = 1) {
                mediator.send(color = parsedColor, from = DomainColorInputType.Rgb)
            }
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator updates data`() =
        runTest(testDispatcher) {
            val rgbColorInputFlow = MutableSharedFlow<ColorInput.Rgb>()
            every { mediator.rgbColorInputFlow } returns rgbColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            ColorInput.Rgb(r = "18", g = "1", b = "20")
                .also { rgbColorInputFlow.emit(it) }

            data.rTextField.text.data.string shouldBe "18"
            data.gTextField.text.data.string shouldBe "1"
            data.bTextField.text.data.string shouldBe "20"
            collectionJob.cancel()
        }

    @Test
    fun `emission from mediator is not sent back to mediator and emission loop is not created`() =
        runTest(testDispatcher) {
            val rgbColorInputFlow = MutableSharedFlow<ColorInput.Rgb>()
            every { mediator.rgbColorInputFlow } returns rgbColorInputFlow
            createSut()
            val collectionJob = launch {
                sut.dataStateFlow.collect() // subscriber to activate the flow
            }

            val sentColorInput = ColorInput.Rgb(r = "18", g = "1", b = "20")
            rgbColorInputFlow.emit(sentColorInput)

            coVerify(exactly = 0) {
                mediator.send(color = any(), from = DomainColorInputType.Rgb)
            }
            collectionJob.cancel()
        }

    @Test
    fun `given 'submit action' returns 'true', when invoking 'submit input', then 'submission result' is emitted`() =
        runTest(testDispatcher) {
            every { submitAction.invoke(colorInput = any(), validationResult = any()) } returns true
            createSut()

            data.submitInput()

            coVerify(exactly = 1) {
                submitAction.invoke(colorInput = any(), validationResult = any())
            }
            val submissionResult = sut.colorSubmissionResultFlow.first()
            submissionResult.wasAccepted shouldBe true
        }

    @Test
    fun `emission of 'Smart Backspace' updates data accordingly`() =
        runTest(testDispatcher) {
            val flowOfSmartBackspace =
                MutableStateFlow(value = DomainSmartBackspace(enabled = false))
            every { userPreferencesRepository.flowOfSmartBackspace } returns flowOfSmartBackspace
            createSut()
            data.isSmartBackspaceEnabled shouldBe false

            flowOfSmartBackspace.value = DomainSmartBackspace(enabled = true)

            data.isSmartBackspaceEnabled shouldBe true
        }

    @ParameterizedTest
    @MethodSource("data")
    fun `user input is filtered as expected`(
        input: String,
        expectedTextString: String,
    ) {
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