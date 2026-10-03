package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import io.github.mmolosay.thecolor.domain.utils.PrefState
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmitAction
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputValidationResult
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbAction
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbData
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbDataFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbViewModel
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
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.SmartBackspace as DomainSmartBackspace

@OptIn(ExperimentalCoroutinesApi::class)
class ColorInputRgbViewModelTest {

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
        every { flowOfSmartBackspace } returns run {
            val value = DomainSmartBackspace(enabled = false)
            val result = PrefState.Result.HasValue(value)
            val prefState = PrefState.Ready(result)
            MutableStateFlow(prefState)
        }
    }

    // real implementation, there's no need to have mock for creating initial data
    val dataFactory = ColorInputRgbDataFactory(
        textFieldDataFactory = TextFieldDataFactory(userPreferencesRepository),
        userPreferencesRepository = userPreferencesRepository,
    )

    val colorInputValidator: ColorInputValidator = mockk {
        every { any<ColorInput.Rgb>().validate() } returns mockk<ColorInputValidationResult.Invalid>()
    }

    val colorInputMapper: ColorInputMapper = mockk()

    val colorConverter: ColorConverter = mockk()

    lateinit var sut: ColorInputRgbViewModel

    @Test
    fun `given mediator has not-null color, when SUT is created, then the text fields are populated with the text of the color`() =
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

            data.rTextField.text.data shouldBe Text("26")
            data.gTextField.text.data shouldBe Text("128")
            data.bTextField.text.data shouldBe Text("63")
        }

    @Test
    fun `given mediator has not-null color, when SUT is created, then the color is NOT set back to mediator, so that the update loop is not created`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorInRgb = Color.Rgb(26, 128, 63)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.ColorState(color = color, source = null, id = 0)
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Rgb("26", "128", "63").validate() } returns
                        ColorInputValidationResult.Valid(colorInRgb)
            }
            with(colorConverter) {
                every { (color as Color).toRgb() } returns colorInRgb
            }
            with(colorInputMapper) {
                every { colorInRgb.toColorInput() } returns ColorInput.Rgb("26", "128", "63")
            }

            createSut()

            coVerify(exactly = 0) {
                mediatorComponents.editor.set(color = any(), source = any())
            }
        }

    @Test // ANCHOR:Label=1
    fun `when text fields are changed to valid color, then the color is set to mediator`() =
        runTest(testDispatcher) {
            val color = Color.Rgb(26, 128, 63)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Rgb("26", "128", "63").validate() } returns
                        ColorInputValidationResult.Valid(color)
            }
            createSut()

            setTextsByUser(r = "26", g = "128", b = "63")

            coVerify(exactly = 1) {
                mediatorComponents.editor.set(
                    color = color,
                    source = ColorInputSource(DomainColorInputType.Rgb),
                )
            }
        }

    @Test
    fun `given text fields contain valid color, when a text field is changed to invalid color, then 'null' color is set to mediator`() =
        runTest(testDispatcher) {
            val color = Color.Rgb(26, 128, 63)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Rgb("26", "128", "63").validate() } returns
                        ColorInputValidationResult.Valid(color)
            }
            createSut()
            setTextsByUser(r = "26", g = "128", b = "63") // REFERENCE:Label=1

            run {
                val action = TextFieldAction.SetText(Text(""))
                sut.executeGTextFieldAction(action).join()
            }

            coVerify(exactly = 1) {
                mediatorComponents.editor.set(
                    color = null,
                    source = ColorInputSource(DomainColorInputType.Rgb),
                )
            }
        }

    @Test
    fun `given text fields contain invalid color, when a text field is changed to another invalid color, then mediator is NOT updated, because the color has not changed`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            createSut()
            run {
                val action = TextFieldAction.SetText(Text("26"))
                sut.executeRTextFieldAction(action).join()
            }

            run {
                val action = TextFieldAction.SetText(Text("128"))
                sut.executeGTextFieldAction(action).join()
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
     * 1. text fields are changed to a valid color, which can't be set to mediator yet
     * 2. a text field is changed, so that text fields contain another valid color
     * 3. mediator's lock is released
     *
     * THEN
     * only the color from WHEN #2 is set to mediator,
     * so that mediator never receives a stale color.
     */
    @Test
    fun `given mediator is locked, when text fields are changed to several valid colors, then only the latest color is set to mediator`() =
        runTest(testDispatcher) {
            // GIVEN
            val firstColor = Color.Rgb(26, 128, 63)
            val latestColor = Color.Rgb(26, 128, 64)
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { ColorInput.Rgb("26", "128", "63").validate() } returns
                        ColorInputValidationResult.Valid(firstColor)
                every { ColorInput.Rgb("26", "128", "64").validate() } returns
                        ColorInputValidationResult.Valid(latestColor)
            }
            val lockRelease = CompletableDeferred<Unit>()
            coEvery { mediator.withLock(block = any()) } coAnswers {
                lockRelease.await()
                val block = firstArg<suspend (ColorInputMediator.Editor) -> Unit>()
                block.invoke(mediatorComponents.editor)
            }
            createSut()

            // WHEN
            setTextsByUser(r = "26", g = "128", b = "63")
            run {
                val action = TextFieldAction.SetText(Text("64"))
                sut.executeBTextFieldAction(action).join()
            }
            lockRelease.complete(Unit)

            // THEN
            coVerify(exactly = 0) {
                mediatorComponents.editor.set(color = firstColor, source = any())
            }
            coVerify(exactly = 1) {
                mediatorComponents.editor.set(
                    color = latestColor,
                    source = ColorInputSource(DomainColorInputType.Rgb),
                )
            }
        }

    @Test
    fun `when mediator emits not-null color from non-RGB source, then the text fields are updated`() =
        runTest(testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val colorInRgb = Color.Rgb(26, 128, 63)
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
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
                    source = ColorInputSource(DomainColorInputType.Hex),
                    id = 1,
                )
                colorStateFlow.emit(value)
            }

            data.rTextField.text.data shouldBe Text("26")
            data.gTextField.text.data shouldBe Text("128")
            data.bTextField.text.data shouldBe Text("63")
        }

    @Test
    fun `when mediator emits not-null color from RGB source, then the text fields are NOT updated, so that the update loop is not created`() =
        runTest(testDispatcher) {
            val color = Color.Rgb(26, 128, 63)
            val colorStateFlow = MutableStateFlow(ColorInputMediator.InitialColorState)
            every { mediator.colorStateFlow } returns colorStateFlow
            createSut()

            run {
                val value = ColorInputMediator.ColorState(
                    color = color,
                    source = ColorInputSource(DomainColorInputType.Rgb),
                    id = 1,
                )
                colorStateFlow.emit(value)
            }

            data.rTextField.text.data shouldBe Text("")
            data.gTextField.text.data shouldBe Text("")
            data.bTextField.text.data shouldBe Text("")
        }

    @Test // ANCHOR:Label=2
    fun `given 'submit action' returns 'true', when 'SubmitInput' action is executed, then data has accepted 'input submission result'`() =
        runTest(testDispatcher) {
            val color = Color.Rgb(26, 128, 63)
            val colorAsColorInput = ColorInput.Rgb("26", "128", "63")
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            with(colorInputValidator) {
                every { colorAsColorInput.validate() } returns ColorInputValidationResult.Valid(color)
            }
            every {
                submitAction.invoke(colorInput = colorAsColorInput, validationResult = any())
            } returns true
            createSut()
            setTextsByUser(r = "26", g = "128", b = "63")

            sut.execute(ColorInputRgbAction.SubmitInput).join()

            data.inputSubmissionResult shouldBe ColorInputSubmissionResult(wasAccepted = true)
        }

    @Test
    fun `given data has 'input submission result', when 'AckInputSubmissionResult' action is executed, then it is cleared from data`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            every { submitAction.invoke(colorInput = any(), validationResult = any()) } returns true
            createSut()
            sut.execute(ColorInputRgbAction.SubmitInput).join() // REFERENCE:Label=2

            sut.execute(ColorInputRgbAction.AckInputSubmissionResult).join()

            data.inputSubmissionResult shouldBe null
        }

    @Test
    fun `when 'select all text on text field focus' preference changes, then all text fields are updated accordingly`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
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
                data.rTextField.shouldSelectAllTextOnFocus shouldBe true
                data.gTextField.shouldSelectAllTextOnFocus shouldBe true
                data.bTextField.shouldSelectAllTextOnFocus shouldBe true
            }

            // WHEN-THEN #2
            run {
                val value = DomainSelectAllTextOnTextFieldFocus(enabled = false)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSelectAllTextOnTextFieldFocus.emit(prefState)
                data.rTextField.shouldSelectAllTextOnFocus shouldBe false
                data.gTextField.shouldSelectAllTextOnFocus shouldBe false
                data.bTextField.shouldSelectAllTextOnFocus shouldBe false
            }
        }

    @Test
    fun `given 'smart backspace' preference is being initialized, when SUT is created, then data has default value for 'is smart backspace enabled'`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            every { userPreferencesRepository.flowOfSmartBackspace } returns run {
                val prefState = PrefState.BeingInitialized
                MutableStateFlow(prefState)
            }

            createSut()

            data.isSmartBackspaceEnabled shouldBe DefaultUserPreferences.SmartBackspace.enabled
        }

    @Test
    fun `when 'smart backspace' preference changes, then data is updated accordingly`() =
        runTest(testDispatcher) {
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            val flowOfSmartBackspace = run {
                val prefState = PrefState.BeingInitialized
                MutableStateFlow<PrefState<DomainSmartBackspace>>(prefState)
            }
            every { userPreferencesRepository.flowOfSmartBackspace } returns flowOfSmartBackspace
            createSut()

            // WHEN-THEN #1
            run {
                val value = DomainSmartBackspace(enabled = false)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSmartBackspace.emit(prefState)
                data.isSmartBackspaceEnabled shouldBe false
            }

            // WHEN-THEN #2
            run {
                val value = DomainSmartBackspace(enabled = true)
                val result = PrefState.Result.HasValue(value)
                val prefState = PrefState.Ready(result)
                flowOfSmartBackspace.emit(prefState)
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
            every { mediator.colorStateFlow } returns run {
                val value = ColorInputMediator.InitialColorState
                MutableStateFlow(value)
            }
            createSut()

            // we check only one component because the logic is same for all 3 of them
            val text = rTextFieldFacade.inputProcessor(input)

            withClue("Filtering user input \"$input\" should return \"$expectedTextString\"") {
                text shouldBe Text(expectedTextString)
            }
        }

    fun createSut() =
        ColorInputRgbViewModel(
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

    val data: ColorInputRgbData
        get() = sut.dataFlow.value

    val rTextFieldFacade: TextFieldFacade
        get() = sut.rTextFieldHandle.facade(data = data.rTextField)

    /**
     * Sets the specified texts to the text fields as if the user has typed them.
     */
    suspend fun setTextsByUser(r: String, g: String, b: String) {
        run {
            val action = TextFieldAction.SetText(Text(r))
            sut.executeRTextFieldAction(action).join()
        }
        run {
            val action = TextFieldAction.SetText(Text(g))
            sut.executeGTextFieldAction(action).join()
        }
        run {
            val action = TextFieldAction.SetText(Text(b))
            sut.executeBTextFieldAction(action).join()
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
