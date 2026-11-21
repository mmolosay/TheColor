package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterCore
import io.github.mmolosay.thecolor.presentation.center.feature.ColorCenterOutcome
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsCore
import io.github.mmolosay.thecolor.presentation.details.feature.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.CreateSessionColorUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeCore
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeState
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorCore
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorOutcome
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupCore
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexCore
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexOutcome
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvCore
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbCore
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewDataFactory
import io.github.mmolosay.thecolor.presentation.scheme.feature.ColorSchemeCore
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

class HomeCoreTest {

    // real implementations, Home's rules are only meaningful with the colors they compare
    val colorConverter = ColorConverter()
    val colorComparator = ColorComparator(colorConverter)

    // real implementation, it's pure and 'toViewData' always uses it
    val colorPreviewDataFactory = ColorPreviewDataFactory(
        colorToColorInt = ColorToColorIntUseCase(colorConverter),
    )

    // only an open session's color uses it, and these tests read none
    val createSessionColor: CreateSessionColorUseCase = mockk()

    val colorEditorFactory = ColorEditorCore.Factory {
        val colorInputMapper = ColorInputMapper()
        val colorInputValidator = ColorInputValidator(
            colorInputMapper = colorInputMapper,
            colorFactory = ColorFactory(ColorPrototypeValidator()),
        )
        val textFieldFactory = TextFieldCore.Factory { formatter ->
            TextFieldCore(formatter)
        }
        ColorEditorCore(
            hexFactory = ColorInputHexCore.Factory {
                ColorInputHexCore(
                    textFieldFactory = textFieldFactory,
                    colorConverter = colorConverter,
                    colorInputMapper = colorInputMapper,
                    colorInputValidator = colorInputValidator,
                )
            },
            rgbFactory = ColorInputRgbCore.Factory {
                ColorInputRgbCore(
                    textFieldFactory = textFieldFactory,
                    colorConverter = colorConverter,
                    colorInputMapper = colorInputMapper,
                    colorInputValidator = colorInputValidator,
                )
            },
            hsvFactory = ColorInputHsvCore.Factory {
                ColorInputHsvCore(
                    colorConverter = colorConverter,
                    colorComparator = colorComparator,
                )
            },
            colorComparator = colorComparator,
        )
    }

    val colorInputGroupFactory = ColorInputGroupCore.Factory {
        ColorInputGroupCore()
    }

    // only the children's 'toViewData' uses their use cases, and these tests don't read it
    val colorDetailsFactory = ColorDetailsCore.Factory {
        ColorDetailsCore(
            createData = mockk(),
            createSubject = mockk(),
        )
    }

    val colorSchemeFactory = ColorSchemeCore.Factory {
        ColorSchemeCore(
            createSwatch = mockk(),
        )
    }

    val colorCenterFactory = ColorCenterCore.Factory {
        ColorCenterCore(
            detailsFactory = colorDetailsFactory,
            schemeFactory = colorSchemeFactory,
        )
    }

    val sut = HomeCore(
        colorEditorFactory = colorEditorFactory,
        colorInputGroupFactory = colorInputGroupFactory,
        colorCenterFactory = colorCenterFactory,
        colorDetailsFactory = colorDetailsFactory,
        colorComparator = colorComparator,
        colorPreviewDataFactory = colorPreviewDataFactory,
        createSessionColor = createSessionColor,
    )

    @Test
    fun `given 'colorEditor' has a color other than the session's, when 'onColorEditor' is invoked, then the session ends`() {
        with(sut) {
            val state = initialState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .withColorEditorColor(Color.Hex(0x126B40))

            val handled = state.onColorEditor(outcome = null)

            handled.state.sessionColor() shouldBe null
        }
    }

    @Test
    fun `given 'colorEditor' has the session's color in another form, when 'onColorEditor' is invoked, then the session stays`() {
        with(sut) {
            val state = initialState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .let {
                    val color = Color.Rgb(r = 26, g = 128, b = 63)
                    it.withColorEditorColor(color)
                }

            val handled = state.onColorEditor(outcome = null)

            handled.state.colorCenter shouldBe state.colorCenter
        }
    }

    @Test
    fun `given 'hex' in 'colorEditor' is submitted with 'FFF', when 'onColorEditor' is invoked with 'ColorSubmitted', then 'hex' is answered with 'Accepted' and a session opens for that color`() {
        with(sut) {
            val submitted = initialState()
                .withHexText("FFF")
                .submitHex()
            val state = submitted.state
            val outcome = submitted.outcome.shouldBeInstanceOf<ColorEditorOutcome.ColorSubmitted>()

            val handled = state.onColorEditor(outcome)

            val submission = handled.state.colorEditor.hex.submission
                .shouldBeInstanceOf<ColorInputSubmission.Answered>()
            submission.answer shouldBe ColorInputSubmission.Answer.Accepted
            handled.state.sessionColor() shouldBe Color.Hex(0xFFFFFF)
        }
    }

    @Test
    fun `given a session is open, when 'onColorCenter' is invoked with 'SessionColorSelected', then 'colorEditor' has that color`() {
        with(sut) {
            val state = initialState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state

            val color = Color.Hex(0x126B40)
            val outcome = ColorCenterOutcome.SessionColorSelected(color)
            val handled = state.onColorCenter(outcome)

            handled.state.colorEditor.color shouldBe color
        }
    }

    @Test
    fun `given a session is open, when 'onColorCenter' is invoked with 'SessionColorSelected', then the session stays`() {
        with(sut) {
            val state = initialState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state

            val outcome = ColorCenterOutcome.SessionColorSelected(Color.Hex(0x126B40))
            val handled = state.onColorCenter(outcome)

            handled.state.colorCenter shouldBe state.colorCenter
        }
    }

    @Test
    fun `given a session is open, when 'onColorCenter' is invoked with 'SwatchSelected', then 'selectedSwatch' is 'Ready' with the swatch's details`() {
        with(sut) {
            val state = initialState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state

            val details = mockk<DomainColorDetails>()
            val handled = state.onColorCenter(ColorCenterOutcome.SwatchSelected(details))

            val swatchAsReady = handled.state.selectedSwatch
                .shouldBeInstanceOf<ColorDetailsState.Ready>()
            swatchAsReady.details shouldBe details
        }
    }

    @Test
    fun `given a swatch is selected, when 'proceed' is invoked, then 'selectedSwatch' is 'NotRequested'`() {
        with(sut) {
            val state = initialState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .let {
                    val outcome = ColorCenterOutcome.SwatchSelected(details = mockk())
                    it.onColorCenter(outcome)
                }.state

            val proceeded = state.proceed()

            proceeded.state.selectedSwatch shouldBe ColorDetailsState.NotRequested
        }
    }

    @Test
    fun `given a swatch is selected, when the session ends, then 'selectedSwatch' is 'NotRequested'`() {
        with(sut) {
            val state = initialState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .let {
                    val outcome = ColorCenterOutcome.SwatchSelected(details = mockk())
                    it.onColorCenter(outcome)
                }.state
                .withColorEditorColor(Color.Hex(0x126B40))

            val handled = state.onColorEditor(outcome = null) // the colors differ, so it ends

            handled.state.selectedSwatch shouldBe ColorDetailsState.NotRequested
        }
    }

    @Test
    fun `when 'completeStartup' is invoked with a color, then 'colorEditor' has that color`() {
        with(sut) {
            val state = initialState()
            val request = state.awaitedStartup().shouldNotBeNull()

            val color = Color.Hex(0x1A803F)
            val completed = state.completeStartup(color, request)

            completed.state.colorEditor.color shouldBe color
        }
    }

    @Test
    fun `when 'completeStartup' is invoked with a color, then a session opens for it`() {
        with(sut) {
            val state = initialState()
            val request = state.awaitedStartup().shouldNotBeNull()

            val color = Color.Hex(0x1A803F)
            val completed = state.completeStartup(color, request)

            completed.state.sessionColor() shouldBe color
        }
    }

    @Test
    fun `given the color in 'colorEditor' changed since the read started, when 'completeStartup' is invoked, then 'colorEditor' keeps its color`() {
        with(sut) {
            val state = initialState()
            val request = state.awaitedStartup().shouldNotBeNull()
            val edited = state.withColorEditorColor(Color.Hex(0x126B40))

            val completed = edited.completeStartup(Color.Hex(0x1A803F), request)

            completed.state.colorEditor.color shouldBe Color.Hex(0x126B40)
        }
    }

    @Test
    fun `given a text with no color was entered in 'hex' since the read started, when 'completeStartup' is invoked, then 'hex' keeps that text`() {
        with(sut) {
            val state = initialState()
            val request = state.awaitedStartup().shouldNotBeNull()
            val edited = state.withHexText("1A") // not a color yet

            val completed = edited.completeStartup(Color.Hex(0x1A803F), request)

            completed.state.colorEditor.hex.textField.text shouldBe "1A"
        }
    }

    @Test
    fun `given the color in 'colorEditor' changed since the read started, when 'colorInputGroup' and 'autoProceed' are in, then the state is initialized`() {
        with(sut) {
            val state = initialState().withColorEditorColor(Color.Hex(0x126B40))

            val applied = state
                .withColorInputGroupPreferredType(DomainColorInputType.Hex)
                .applyAutoProceed(enabled = false).state

            applied.isInitialized() shouldBe true
        }
    }

    @Test
    fun `given the startup color and 'colorInputGroup' are in, when 'isInitialized' is invoked before 'applyAutoProceed', then it's 'false'`() {
        with(sut) {
            val state = initialState()
                .let { state ->
                    val request = state.awaitedStartup().shouldNotBeNull()
                    state.completeStartup(Color.Hex(0x1A803F), request)
                }.state
                .withColorInputGroupPreferredType(DomainColorInputType.Hex)

            val isInitialized = state.isInitialized()

            isInitialized shouldBe false
        }
    }

    @Test
    fun `given 'autoProceed' is off, when 'randomize' is invoked with another color, then the session ends`() {
        with(sut) {
            val state = initialState()
                .applyAutoProceed(enabled = false).state
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state

            val randomized = state.randomize(Color.Hex(0x126B40))

            randomized.state.sessionColor() shouldBe null
        }
    }

    @Test
    fun `given 'autoProceed' is on, when 'randomize' is invoked, then a session opens for that color`() {
        with(sut) {
            val state = initialState().applyAutoProceed(enabled = true).state

            val color = Color.Hex(0x1A803F)
            val randomized = state.randomize(color)

            randomized.state.sessionColor() shouldBe color
        }
    }

    @Test
    fun `given 'colorEditor' has a color, when 'proceed' is invoked, then a save of that color is awaited`() {
        with(sut) {
            val color = Color.Hex(0x1A803F)
            val state = initialState().withColorEditorColor(color)

            val proceeded = state.proceed()

            val awaited = proceeded.state.awaitedSaveRequest().shouldNotBeNull()
            awaited.color shouldBe color
        }
    }

    @Test
    fun `given a newer save is awaited, when 'completeSave' is invoked for the older one, then the newer one is still awaited`() {
        with(sut) {
            val older = initialState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state
            val olderSave = older.awaitedSaveRequest().shouldNotBeNull()
            val newer = older.proceed().state

            val completed = newer.completeSave(olderSave)

            completed.state.awaitedSaveRequest() shouldBe newer.awaitedSaveRequest()
        }
    }

    @Test
    fun `given a session is open, when it ends, then 'sessionColor' is 'null'`() {
        with(sut) {
            val state = initializedState()
                .withColorEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .withColorEditorColor(Color.Hex(0x126B40))

            val handled = state.onColorEditor(outcome = null) // the colors differ, so it ends

            handled.state.toViewData().shouldNotBeNull().sessionColor shouldBe null
        }
    }

    @Test
    fun `given 'colorEditor' has no color, when 'toViewData' is invoked, then 'canProceed' is 'false'`() {
        with(sut) {
            val state = initializedState()

            val viewData = state.toViewData()

            viewData.shouldNotBeNull().canProceed shouldBe false
        }
    }

    fun initializedState(): HomeState =
        with(sut) {
            val state = initialState()
            val request = state.awaitedStartup().shouldNotBeNull()
            state
                .completeStartup(color = null, expected = request).state
                .withColorInputGroupPreferredType(DomainColorInputType.Hex)
                .applyAutoProceed(enabled = false).state
        }

    fun HomeState.sessionColor(): Color? =
        with(sut.colorCenterCore) { colorCenter.sessionColor() }

    fun HomeState.withColorEditorColor(color: Color): HomeState {
        val edited = with(sut.colorEditorCore) { colorEditor.withColor(color) }
        return this.copy(colorEditor = edited)
    }

    fun HomeState.withHexText(text: String): HomeState {
        val editor = this.colorEditor
        val newHex = editor.hex.enterText(text)
        val newEditor = with(sut.colorEditorCore) {
            editor
                .copy(hex = newHex)
                .onHex(outcome = null)
        }.state
        return this.copy(colorEditor = newEditor)
    }

    fun HomeState.submitHex(): Change<HomeState, ColorEditorOutcome> {
        val editor = this.colorEditor
        val newHex = with(sut.colorEditorCore.hexCore) { editor.hex.submit() }.state
        val newEditor = with(sut.colorEditorCore) {
            editor
                .copy(hex = newHex)
                .onHex(ColorInputHexOutcome.Submitted)
        }
        val state = this.copy(colorEditor = newEditor.state)
        return Change(state, newEditor.outcome)
    }

    fun ColorInputHexState.enterText(text: String): ColorInputHexState {
        val textField = this.textField
        val newTextField = with(sut.colorEditorCore.hexCore.textFieldCore) {
            textField.enterText(text, expected = textField.text)
        }.state
        return this.copy(textField = newTextField)
    }

    fun HomeState.withColorInputGroupPreferredType(type: DomainColorInputType): HomeState {
        val applied = with(sut.colorInputGroupCore) {
            colorInputGroup.applyPreferredType(type).state
        }
        return this.copy(colorInputGroup = applied)
    }
}
