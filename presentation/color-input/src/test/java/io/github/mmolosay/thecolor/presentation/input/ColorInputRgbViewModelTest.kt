package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbAction
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.github.mmolosay.thecolor.utils.Atom
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SelectAllTextOnTextFieldFocus as DomainSelectAllTextOnTextFieldFocus
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace as DomainSmartBackspace

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputRgbViewModelTest {

    val testDispatcher = UnconfinedTestDispatcher()

    @RegisterExtension
    @Suppress("unused")
    val mainDispatcherExtension = MainDispatcherExtension(testDispatcher)

    val submitAction: ColorInputSubmitAction = mockk()

    val userPreferencesRepository: UserPreferencesRepository = mockk {
        every { flowOfSelectAllTextOnTextFieldFocus } returns run {
            val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
            val result = PrefState.Result.HasValue(value)
            val prefState = PrefState.Ready(result)
            MutableStateFlow(prefState)
        }
        every { flowOfSmartBackspace } returns run {
            val value = DomainSmartBackspace(enabled = false)
            val result = PrefState.Result.HasValue(value)
            val prefState = PrefState.Ready(result)
            MutableStateFlow(prefState)
        }
    }

    // real implementation, there's no need to have mock for validating color input
    val colorInputValidator = ColorInputValidator(
        colorInputMapper = ColorInputMapper(),
        colorFactory = ColorFactory(ColorPrototypeValidator()),
    )

    lateinit var sut: ColorInputRgbViewModel

    @Test
    fun `when 'SetText' expects the texts the fields hold, then the texts are applied as typed by the user`() =
        runTest(testDispatcher) {
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputRgbState(r = "", g = "", b = "")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            setTextsByUser(stateFlow, r = "26", g = "128", b = "63")

            val state = stateFlow.value
            state.rTextField.text shouldBe (Text("26") causedByUser true)
            state.gTextField.text shouldBe (Text("128") causedByUser true)
            state.bTextField.text shouldBe (Text("63") causedByUser true)
        }

    @Test
    fun `when 'SetText' expects a text the field no longer holds, then the text is NOT changed`() =
        runTest(testDispatcher) {
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputRgbState(r = "255", g = "255", b = "255")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            run {
                val action = TextFieldAction.SetText(text = Text("1280"), expected = Text("128"))
                sut.executeGTextFieldAction(action)
            }

            stateFlow.value.gTextField.text.data shouldBe Text("255")
        }

    @Test
    fun `when 'SubmitInput' action is executed, then the current input is submitted`() =
        runTest(testDispatcher) {
            every { submitAction.invoke(colorInput = any(), validationResult = any()) } returns true
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputRgbState(r = "26", g = "128", b = "63")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            sut.execute(ColorInputRgbAction.SubmitInput)

            verify(exactly = 1) {
                submitAction.invoke(
                    colorInput = ColorInput.Rgb(r = "26", g = "128", b = "63"),
                    validationResult = any(),
                )
            }
        }

    @Test
    fun `given 'submit action' returns 'true', when 'SubmitInput' action is executed, then state has accepted 'input submission result'`() =
        runTest(testDispatcher) {
            every { submitAction.invoke(colorInput = any(), validationResult = any()) } returns true
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputRgbState(r = "26", g = "128", b = "63")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            sut.execute(ColorInputRgbAction.SubmitInput)

            stateFlow.value.inputSubmissionResult shouldBe ColorInputSubmissionResult(wasAccepted = true)
        }

    @Test
    fun `given state has 'input submission result', when 'AckInputSubmissionResult' action is executed, then it is cleared`() =
        runTest(testDispatcher) {
            val stateFlow = run {
                val result = ColorInputSubmissionResult(wasAccepted = true)
                val state = MockColorInputStates.ColorInputRgbState(r = "26", g = "128", b = "63")
                MutableStateFlow(state.copy(inputSubmissionResult = result))
            }
            createSut(stateFlow)

            sut.execute(ColorInputRgbAction.AckInputSubmissionResult)

            stateFlow.value.inputSubmissionResult shouldBe null
        }

    @Test
    fun `when 'select all text on text field focus' preference changes, then all text fields are updated accordingly`() =
        runTest(testDispatcher) {
            val flowOfSelectAllTextOnTextFieldFocus = run {
                val prefState = PrefState.BeingInitialized
                MutableStateFlow<PrefState<DomainSelectAllTextOnTextFieldFocus>>(prefState)
            }
            every {
                userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
            } returns flowOfSelectAllTextOnTextFieldFocus
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputRgbState(r = "", g = "", b = "")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            // WHEN-THEN #1
            run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = true)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSelectAllTextOnTextFieldFocus.emit(prefState)
                val state = stateFlow.value
                state.rTextField.shouldSelectAllTextOnFocus shouldBe true
                state.gTextField.shouldSelectAllTextOnFocus shouldBe true
                state.bTextField.shouldSelectAllTextOnFocus shouldBe true
            }

            // WHEN-THEN #2
            run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSelectAllTextOnTextFieldFocus.emit(prefState)
                val state = stateFlow.value
                state.rTextField.shouldSelectAllTextOnFocus shouldBe false
                state.gTextField.shouldSelectAllTextOnFocus shouldBe false
                state.bTextField.shouldSelectAllTextOnFocus shouldBe false
            }
        }

    @Test
    fun `when 'smart backspace' preference changes, then state is updated accordingly`() =
        runTest(testDispatcher) {
            val flowOfSmartBackspace = run {
                val prefState = PrefState.BeingInitialized
                MutableStateFlow<PrefState<DomainSmartBackspace>>(prefState)
            }
            every { userPreferencesRepository.flowOfSmartBackspace } returns flowOfSmartBackspace
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputRgbState(r = "", g = "", b = "")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            // WHEN-THEN #1
            run {
                val value = DomainSmartBackspace(enabled = true)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSmartBackspace.emit(prefState)
                stateFlow.value.isSmartBackspaceEnabled shouldBe true
            }

            // WHEN-THEN #2
            run {
                val value = DomainSmartBackspace(enabled = false)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSmartBackspace.emit(prefState)
                stateFlow.value.isSmartBackspaceEnabled shouldBe false
            }
        }

    @ParameterizedTest
    @MethodSource("data")
    fun `user input is filtered as expected`(
        input: String,
        expectedTextString: String,
    ) =
        runTest(testDispatcher) {
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputRgbState(r = "", g = "", b = "")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            // only one component is checked, because the logic is the same for all three
            val text = sut.rTextFieldHandle.facade(data = stateFlow.value.rTextField).inputProcessor(input)

            withClue("Filtering user input \"$input\" should return \"$expectedTextString\"") {
                text shouldBe Text(expectedTextString)
            }
        }

    fun createSut(
        stateFlow: MutableStateFlow<ColorInputRgbState>,
    ) =
        ColorInputRgbViewModel(
            coroutineScope = CoroutineScope(context = testDispatcher),
            atom = Atom(stateFlow),
            submitAction = submitAction,
            colorInputValidator = colorInputValidator,
            userPreferencesRepository = userPreferencesRepository,
        ).also {
            sut = it
        }

    /** Sets the texts as if the user typed them, each into its field while it held the text it holds now. */
    fun setTextsByUser(
        stateFlow: MutableStateFlow<ColorInputRgbState>,
        r: String,
        g: String,
        b: String,
    ) {
        run {
            val expected = stateFlow.value.rTextField.text.data
            sut.executeRTextFieldAction(TextFieldAction.SetText(text = Text(r), expected = expected))
        }
        run {
            val expected = stateFlow.value.gTextField.text.data
            sut.executeGTextFieldAction(TextFieldAction.SetText(text = Text(g), expected = expected))
        }
        run {
            val expected = stateFlow.value.bTextField.text.data
            sut.executeBTextFieldAction(TextFieldAction.SetText(text = Text(b), expected = expected))
        }
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
