package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexAction
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
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

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputHexViewModelTest {

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
    }

    // real implementation, there's no need to have mock for validating color input
    val colorInputValidator = ColorInputValidator(
        colorInputMapper = ColorInputMapper(),
        colorFactory = ColorFactory(ColorPrototypeValidator()),
    )

    lateinit var sut: ColorInputHexViewModel

    @Test
    fun `when 'SetText' expects the text the field holds, then the text is applied as typed by the user`() =
        runTest(testDispatcher) {
            val stateFlow = MutableStateFlow(MockColorInputStates.ColorInputHexState(text = ""))
            createSut(stateFlow)

            run {
                val action = TextFieldAction.SetText(text = Text("1A803F"), expected = Text(""))
                sut.executeTextFieldAction(action)
            }

            stateFlow.value.textField.text shouldBe (Text("1A803F") causedByUser true)
        }

    @Test
    fun `when 'SetText' expects a text the field no longer holds, then the text is NOT changed`() =
        runTest(testDispatcher) {
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputHexState(text = "FFFFFF")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            run {
                val action = TextFieldAction.SetText(text = Text("F0A"), expected = Text("F0"))
                sut.executeTextFieldAction(action)
            }

            stateFlow.value.textField.text.data shouldBe Text("FFFFFF")
        }

    @Test
    fun `when 'SubmitInput' action is executed, then the current input is submitted`() =
        runTest(testDispatcher) {
            every { submitAction.invoke(colorInput = any(), validationResult = any()) } returns true
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputHexState(text = "1A803F")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            sut.execute(ColorInputHexAction.SubmitInput)

            verify(exactly = 1) {
                submitAction.invoke(colorInput = ColorInput.Hex("1A803F"), validationResult = any())
            }
        }

    @Test
    fun `given 'submit action' returns 'true', when 'SubmitInput' action is executed, then state has accepted 'input submission result'`() =
        runTest(testDispatcher) {
            every { submitAction.invoke(colorInput = any(), validationResult = any()) } returns true
            val stateFlow = run {
                val state = MockColorInputStates.ColorInputHexState(text = "1A803F")
                MutableStateFlow(state)
            }
            createSut(stateFlow)

            sut.execute(ColorInputHexAction.SubmitInput)

            stateFlow.value.inputSubmissionResult shouldBe ColorInputSubmissionResult(wasAccepted = true)
        }

    @Test
    fun `given 'submit action' returns 'false', when 'SubmitInput' action is executed, then state has rejected 'input submission result'`() =
        runTest(testDispatcher) {
            every {
                submitAction.invoke(
                    colorInput = any(),
                    validationResult = any()
                )
            } returns false
            val stateFlow = MutableStateFlow(MockColorInputStates.ColorInputHexState(text = "1A80"))
            createSut(stateFlow)

            sut.execute(ColorInputHexAction.SubmitInput)

            stateFlow.value.inputSubmissionResult shouldBe ColorInputSubmissionResult(wasAccepted = false)
        }

    @Test
    fun `given state has 'input submission result', when 'AckInputSubmissionResult' action is executed, then it is cleared`() =
        runTest(testDispatcher) {
            val stateFlow = run {
                val result = ColorInputSubmissionResult(wasAccepted = true)
                val state = MockColorInputStates.ColorInputHexState(text = "1A803F")
                MutableStateFlow(state.copy(inputSubmissionResult = result))
            }
            createSut(stateFlow)

            sut.execute(ColorInputHexAction.AckInputSubmissionResult)

            stateFlow.value.inputSubmissionResult shouldBe null
        }

    @Test
    fun `when 'select all text on text field focus' preference changes, then the text field is updated accordingly`() =
        runTest(testDispatcher) {
            val flowOfSelectAllTextOnTextFieldFocus = run {
                val prefState = PrefState.BeingInitialized
                MutableStateFlow<PrefState<DomainSelectAllTextOnTextFieldFocus>>(prefState)
            }
            every {
                userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
            } returns flowOfSelectAllTextOnTextFieldFocus
            val stateFlow = MutableStateFlow(MockColorInputStates.ColorInputHexState(text = ""))
            createSut(stateFlow)

            // WHEN-THEN #1
            run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = true)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSelectAllTextOnTextFieldFocus.emit(prefState)
                stateFlow.value.textField.shouldSelectAllTextOnFocus shouldBe true
            }

            // WHEN-THEN #2
            run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSelectAllTextOnTextFieldFocus.emit(prefState)
                stateFlow.value.textField.shouldSelectAllTextOnFocus shouldBe false
            }
        }

    @ParameterizedTest
    @MethodSource("data")
    fun `user input is filtered as expected`(
        input: String,
        expectedTextString: String,
    ) =
        runTest(testDispatcher) {
            val stateFlow = MutableStateFlow(MockColorInputStates.ColorInputHexState(text = ""))
            createSut(stateFlow)

            val text =
                sut.textFieldHandle.facade(data = stateFlow.value.textField).inputProcessor(input)

            withClue("Filtering user input \"$input\" should return \"$expectedTextString\"") {
                text shouldBe Text(expectedTextString)
            }
        }

    fun createSut(
        stateFlow: MutableStateFlow<ColorInputHexState>,
    ) =
        ColorInputHexViewModel(
            coroutineScope = CoroutineScope(context = testDispatcher),
            atom = Atom(stateFlow),
            submitAction = submitAction,
            colorInputValidator = colorInputValidator,
            userPreferencesRepository = userPreferencesRepository,
        ).also {
            sut = it
        }

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
