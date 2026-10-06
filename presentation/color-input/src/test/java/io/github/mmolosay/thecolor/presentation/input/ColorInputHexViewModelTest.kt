package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexAction
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexData
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexDataFactory
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.testing.EmptyColorState
import io.github.mmolosay.thecolor.presentation.input.testing.MockColorInputMediatorComponents
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldDataFactory
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.testing.MainDispatcherExtension
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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

    // can't be unconfined, because SUT derives a dispatcher with limited parallelism from it
    val defaultDispatcher = StandardTestDispatcher(testDispatcher.scheduler)

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

    // real implementation, there's no need to have mock for creating initial data
    val dataFactory = ColorInputHexDataFactory(
        textFieldDataFactory = TextFieldDataFactory(userPreferencesRepository),
    )

    val colorInputValidator: ColorInputValidator = mockk {
        every { any<ColorInput.Hex>().validate() } returns mockk<ColorInputValidationResult.Invalid>()
    }

    val colorInputMapper: ColorInputMapper = mockk()

    val colorConverter: ColorConverter = mockk()

    lateinit var sut: ColorInputHexViewModel

    @Test
    fun `given mediator has not-null color, when SUT is created, then the text field is populated with the text of the color`() =
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

            data.textField.text.data shouldBe Text("1A803F")
        }

    @Test
    fun `given mediator has not-null color, when SUT is created, then the color is NOT set back to mediator, so that the update loop is not created`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Hex("1A803F").validate() } returns ColorInputValidationResult.Valid(color)
            }
            with(colorConverter) {
                every { (color as Color).toHex() } returns color
            }
            with(colorInputMapper) {
                every { color.toColorInput() } returns ColorInput.Hex("1A803F")
            }

            createSut()

            coVerify(exactly = 0) {
                mediatorComponents.editor.set(color = any(), source = any())
            }
        }

    @Test // ANCHOR:Label=1
    fun `when text is changed to valid color, then the color is set to mediator`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            every { mediator.colorStateFlow } returns run {
                val value = EmptyColorState
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Hex("1A803F").validate() } returns ColorInputValidationResult.Valid(color)
            }
            createSut()

            run {
                val action = TextFieldAction.SetText(Text("1A803F"))
                sut.executeTextFieldAction(action).join()
            }

            coVerify(exactly = 1) {
                mediatorComponents.editor.set(
                    color = color,
                    source = ColorInputSource(DomainColorInputType.Hex),
                )
            }
        }

    @Test
    fun `given text is valid color, when text is changed to invalid color, then 'null' color is set to mediator`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            every { mediator.colorStateFlow } returns run {
                val value = EmptyColorState
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Hex("1A803F").validate() } returns ColorInputValidationResult.Valid(color)
            }
            createSut()
            run {
                val action = TextFieldAction.SetText(Text("1A803F"))
                sut.executeTextFieldAction(action).join() // REFERENCE:Label=1
            }

            run {
                val action = TextFieldAction.SetText(Text("1A80"))
                sut.executeTextFieldAction(action).join()
            }

            coVerify(exactly = 1) {
                mediatorComponents.editor.set(
                    color = null,
                    source = ColorInputSource(DomainColorInputType.Hex),
                )
            }
        }

    @Test
    fun `given text is invalid color, when text is changed to another invalid color, then mediator is NOT updated, because the color has not changed`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = EmptyColorState
                MutableStateFlow(value)
            }
            createSut()
            run {
                val action = TextFieldAction.SetText(Text("1A"))
                sut.executeTextFieldAction(action).join()
            }

            run {
                val action = TextFieldAction.SetText(Text("1A80"))
                sut.executeTextFieldAction(action).join()
            }

            coVerify(exactly = 0) {
                mediatorComponents.editor.set(color = any(), source = any())
            }
        }

    /**
     * GIVEN
     * 1. [sut] is created
     * 2. mediator's lock is held by someone else, so setting a color to mediator suspends
     *
     * WHEN
     * 1. text is changed to a valid color, which can't be set to mediator yet
     * 2. text is changed to another valid color
     * 3. mediator's lock is released
     *
     * THEN
     * only the color from WHEN #2 is set to mediator,
     * so that mediator never receives a stale color.
     */
    @Test
    fun `given mediator is locked, when text is changed to several valid colors, then only the latest color is set to mediator`() =
        runTest(testDispatcher) {
            // GIVEN
            val firstColor = Color.Hex(0x1A803F)
            val latestColor = Color.Hex(0x123456)
            every { mediator.colorStateFlow } returns run {
                val value = EmptyColorState
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Hex("1A803F").validate() } returns ColorInputValidationResult.Valid(firstColor)
                every { ColorInput.Hex("123456").validate() } returns ColorInputValidationResult.Valid(latestColor)
            }
            val lockRelease = CompletableDeferred<Unit>()
            coEvery { mediator.withLock(block = any()) } coAnswers {
                lockRelease.await()
                val block = firstArg<suspend (ColorInputMediator.Editor) -> Unit>()
                block.invoke(mediatorComponents.editor)
            }
            createSut()

            // WHEN
            run {
                val action = TextFieldAction.SetText(Text("1A803F"))
                sut.executeTextFieldAction(action).join()
            }
            run {
                val action = TextFieldAction.SetText(Text("123456"))
                sut.executeTextFieldAction(action).join()
            }
            lockRelease.complete(Unit)

            // THEN
            coVerify(exactly = 0) {
                mediatorComponents.editor.set(color = firstColor, source = any())
            }
            coVerify(exactly = 1) {
                mediatorComponents.editor.set(
                    color = latestColor,
                    source = ColorInputSource(DomainColorInputType.Hex),
                )
            }
        }

    @Test
    fun `when mediator emits not-null color from non-HEX source, then the text field is updated`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorStateFlow = MutableStateFlow(EmptyColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
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
                val value = ColorInputMediator.ColorState(
                    color = color,
                    source = ColorInputSource(DomainColorInputType.Rgb),
                    id = 1,
                )
                colorStateFlow.emit(value)
            }

            data.textField.text.data shouldBe Text("1A803F")
        }

    @Test
    fun `given the text field has text, when mediator emits 'null' color from non-HEX source, then the text field is cleared`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorStateFlow = MutableStateFlow(EmptyColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            with(colorInputValidator) {
                every { ColorInput.Hex("1A803F").validate() } returns ColorInputValidationResult.Valid(color)
            }
            with(colorConverter) {
                every { (color as Color).toHex() } returns color
            }
            with(colorInputMapper) {
                every { color.toColorInput() } returns ColorInput.Hex("1A803F")
            }
            createSut()
            run emitColorFromRgb@{
                val value = ColorInputMediator.ColorState(
                    color = color,
                    source = ColorInputSource(DomainColorInputType.Rgb),
                    id = 1,
                )
                colorStateFlow.emit(value)
            }
            data.textField.text.data shouldBe Text("1A803F") // assumption of the test

            run {
                val value = ColorInputMediator.ColorState(
                    color = null,
                    source = ColorInputSource(DomainColorInputType.Rgb),
                    id = 2,
                )
                colorStateFlow.emit(value)
            }

            data.textField.text.data shouldBe Text("")
        }

    @Test
    fun `when mediator emits not-null color from HEX source, then the text field is NOT updated, so that the update loop is not created`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorStateFlow = MutableStateFlow(EmptyColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            createSut()

            run {
                val value = ColorInputMediator.ColorState(
                    color = color,
                    source = ColorInputSource(DomainColorInputType.Hex),
                    id = 1,
                )
                colorStateFlow.emit(value)
            }

            data.textField.text.data shouldBe Text("")
        }

    @Test
    fun `when 'select all text on text field focus' preference changes, then the text field data is updated accordingly`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = EmptyColorState
                MutableStateFlow(value)
            }
            val flowOfSelectAllTextOnTextFieldFocus = run {
                val prefState = PrefState.BeingInitialized
                MutableStateFlow<PrefState<DomainSelectAllTextOnTextFieldFocus>>(prefState)
            }
            every {
                userPreferencesRepository.flowOfSelectAllTextOnTextFieldFocus
            } returns flowOfSelectAllTextOnTextFieldFocus
            createSut()

            // WHEN-THEN #1
            run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = true)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSelectAllTextOnTextFieldFocus.emit(prefState)
                data.textField.shouldSelectAllTextOnFocus shouldBe true
            }

            // WHEN-THEN #2
            run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSelectAllTextOnTextFieldFocus.emit(prefState)
                data.textField.shouldSelectAllTextOnFocus shouldBe false
            }
        }

    @Test // ANCHOR:Label=2
    fun `given 'submit action' returns 'true', when 'SubmitInput' action is executed, then data has accepted 'input submission result'`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorAsColorInput = ColorInput.Hex("1A803F")
            every { mediator.colorStateFlow } returns run {
                val value = EmptyColorState
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { colorAsColorInput.validate() } returns ColorInputValidationResult.Valid(color)
            }
            every {
                submitAction.invoke(colorInput = colorAsColorInput, validationResult = any())
            } returns true
            createSut()
            run {
                val action = TextFieldAction.SetText(Text("1A803F"))
                sut.executeTextFieldAction(action).join()
            }

            sut.execute(ColorInputHexAction.SubmitInput).join()

            data.inputSubmissionResult shouldBe ColorInputSubmissionResult(wasAccepted = true)
        }

    @Test
    fun `given data has 'input submission result', when 'AckInputSubmissionResult' action is executed, then it is cleared from data`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = EmptyColorState
                MutableStateFlow(value)
            }
            every { submitAction.invoke(colorInput = any(), validationResult = any()) } returns true
            createSut()
            sut.execute(ColorInputHexAction.SubmitInput).join() // REFERENCE:Label=2

            sut.execute(ColorInputHexAction.AckInputSubmissionResult).join()

            data.inputSubmissionResult shouldBe null
        }

    @ParameterizedTest
    @MethodSource("data")
    fun `user input is filtered as expected`(
        input: String,
        expectedTextString: String,
    ) =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = EmptyColorState
                MutableStateFlow(value)
            }
            createSut()

            val text = textFieldFacade.inputProcessor(input)

            withClue("Filtering user input \"$input\" should return \"$expectedTextString\"") {
                text shouldBe Text(expectedTextString)
            }
        }

    fun createSut() =
        ColorInputHexViewModel(
            coroutineScope = CoroutineScope(context = testDispatcher),
            mediator = mediator,
            submitAction = submitAction,
            dataFactory = dataFactory,
            colorInputValidator = colorInputValidator,
            colorInputMapper = colorInputMapper,
            colorConverter = colorConverter,
            userPreferencesRepository = userPreferencesRepository,
            defaultDispatcher = defaultDispatcher,
        ).also {
            sut = it
        }

    val data: ColorInputHexData
        get() = sut.dataFlow.value

    val textFieldFacade: TextFieldFacade
        get() = sut.textFieldHandle.facade(data = data.textField)

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
