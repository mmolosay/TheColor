package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterCore
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterOutcome
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsCore
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.CreateColorDataUseCase
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeCore
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeState
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorCore
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupCore
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldDataFactory
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewDataFactory
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeCore
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

    // only 'colorPreview' and a 'Success' proceed result use them, and these tests read neither
    val colorPreviewDataFactory: ColorPreviewDataFactory = mockk()
    val createColorData: CreateColorDataUseCase = mockk()

    val inputEditorFactory = ColorEditorCore.Factory {
        val colorInputMapper = ColorInputMapper()
        val textFieldDataFactory = TextFieldDataFactory()
        ColorEditorCore(
            colorConverter = colorConverter,
            colorInputMapper = colorInputMapper,
            colorInputValidator = ColorInputValidator(
                colorInputMapper = colorInputMapper,
                colorFactory = ColorFactory(ColorPrototypeValidator()),
            ),
            colorComparator = colorComparator,
            hexStateFactory = ColorInputHexStateFactory(
                textFieldDataFactory = textFieldDataFactory,
                colorConverter = colorConverter,
                colorInputMapper = colorInputMapper,
            ),
            rgbStateFactory = ColorInputRgbStateFactory(
                textFieldDataFactory = textFieldDataFactory,
                colorConverter = colorConverter,
                colorInputMapper = colorInputMapper,
            ),
            hsvStateFactory = ColorInputHsvStateFactory(
                colorConverter = colorConverter,
            ),
        )
    }

    val inputGroupFactory = ColorInputGroupCore.Factory {
        ColorInputGroupCore()
    }

    // only 'toViewData' uses their use cases, and these tests don't read view data
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
        inputEditorFactory = inputEditorFactory,
        inputGroupFactory = inputGroupFactory,
        colorCenterFactory = colorCenterFactory,
        colorDetailsFactory = colorDetailsFactory,
        colorComparator = colorComparator,
        colorPreviewDataFactory = colorPreviewDataFactory,
        createColorData = createColorData,
    )

    @Test
    fun `given 'inputEditor' has a color other than the session's, when 'onInputEditor' is invoked, then the session ends`() {
        with(sut) {
            val state = initialState()
                .withInputEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .withInputEditorColor(Color.Hex(0x126B40))

            val handled = state.onInputEditor()

            handled.state.sessionColor() shouldBe null
        }
    }

    @Test
    fun `given 'inputEditor' has the session's color in another form, when 'onInputEditor' is invoked, then the session stays`() {
        with(sut) {
            val state = initialState()
                .withInputEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .let {
                    val color = Color.Rgb(r = 26, g = 128, b = 63)
                    it.withInputEditorColor(color)
                }

            val handled = state.onInputEditor()

            handled.state.colorCenter shouldBe state.colorCenter
        }
    }

    @Test
    fun `given a session is open, when 'onColorCenter' is invoked with 'SessionColorSelected', then 'inputEditor' has that color`() {
        with(sut) {
            val state = initialState()
                .withInputEditorColor(Color.Hex(0x1A803F))
                .proceed().state

            val color = Color.Hex(0x126B40)
            val outcome = ColorCenterOutcome.SessionColorSelected(color)
            val handled = state.onColorCenter(outcome)

            handled.state.inputEditor.color shouldBe color
        }
    }

    @Test
    fun `given a session is open, when 'onColorCenter' is invoked with 'SessionColorSelected', then the session stays`() {
        with(sut) {
            val state = initialState()
                .withInputEditorColor(Color.Hex(0x1A803F))
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
                .withInputEditorColor(Color.Hex(0x1A803F))
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
                .withInputEditorColor(Color.Hex(0x1A803F))
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
                .withInputEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .let {
                    val outcome = ColorCenterOutcome.SwatchSelected(details = mockk())
                    it.onColorCenter(outcome)
                }.state
                .withInputEditorColor(Color.Hex(0x126B40))

            val handled = state.onInputEditor() // the colors differ, so the session ends

            handled.state.selectedSwatch shouldBe ColorDetailsState.NotRequested
        }
    }

    @Test
    fun `when 'completeStartup' is invoked with a color, then 'inputEditor' has that color`() {
        with(sut) {
            val state = initialState()
            val request = state.awaitedStartup().shouldNotBeNull()

            val color = Color.Hex(0x1A803F)
            val completed = state.completeStartup(color, request)

            completed.state.inputEditor.color shouldBe color
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
    fun `given the color in 'inputEditor' changed since the read started, when 'completeStartup' is invoked, then 'inputEditor' keeps its color`() {
        with(sut) {
            val state = initialState()
            val request = state.awaitedStartup().shouldNotBeNull()
            val edited = state.withInputEditorColor(Color.Hex(0x126B40))

            val completed = edited.completeStartup(Color.Hex(0x1A803F), request)

            completed.state.inputEditor.color shouldBe Color.Hex(0x126B40)
        }
    }

    @Test
    fun `given the color in 'inputEditor' changed since the read started, when 'inputGroup' and 'autoProceed' are in, then the state is initialized`() {
        with(sut) {
            val state = initialState().withInputEditorColor(Color.Hex(0x126B40))

            val applied = state
                .withInputGroupPreferredType(DomainColorInputType.Hex)
                .applyAutoProceed(enabled = false).state

            applied.isInitialized() shouldBe true
        }
    }

    @Test
    fun `given the startup color and 'inputGroup' are in, when 'isInitialized' is invoked before 'applyAutoProceed', then it's 'false'`() {
        with(sut) {
            val state = initialState()
                .let { state ->
                    val request = state.awaitedStartup().shouldNotBeNull()
                    state.completeStartup(Color.Hex(0x1A803F), request)
                }.state
                .withInputGroupPreferredType(DomainColorInputType.Hex)

            val isInitialized = state.isInitialized()

            isInitialized shouldBe false
        }
    }

    @Test
    fun `given 'autoProceed' is off, when 'randomize' is invoked with another color, then the session ends`() {
        with(sut) {
            val state = initialState()
                .applyAutoProceed(enabled = false).state
                .withInputEditorColor(Color.Hex(0x1A803F))
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
    fun `given 'inputEditor' has a color, when 'proceed' is invoked, then a save of that color is awaited`() {
        with(sut) {
            val color = Color.Hex(0x1A803F)
            val state = initialState().withInputEditorColor(color)

            val proceeded = state.proceed()

            val awaited = proceeded.state.awaitedSave().shouldNotBeNull()
            awaited.color shouldBe color
        }
    }

    @Test
    fun `given a newer save is awaited, when 'completeSave' is invoked for the older one, then the newer one is still awaited`() {
        with(sut) {
            val older = initialState()
                .withInputEditorColor(Color.Hex(0x1A803F))
                .proceed().state
            val olderSave = older.awaitedSave().shouldNotBeNull()
            val newer = older.proceed().state

            val completed = newer.completeSave(olderSave)

            completed.state.awaitedSave() shouldBe newer.awaitedSave()
        }
    }

    @Test
    fun `given a session is open, when it ends, then the proceed result is 'null'`() {
        with(sut) {
            val state = initialState()
                .withInputEditorColor(Color.Hex(0x1A803F))
                .proceed().state
                .withInputEditorColor(Color.Hex(0x126B40))

            val handled = state.onInputEditor() // the colors differ, so the session ends

            handled.state.data().proceedResult shouldBe null
        }
    }

    fun HomeState.sessionColor(): Color? =
        with(sut.colorCenterCore) { colorCenter.sessionColor() }

    // the editor's color changes, as an edit in one of its inputs changes it
    fun HomeState.withInputEditorColor(color: Color): HomeState {
        val edited = with(sut.inputEditorCore) { inputEditor.withColor(color) }
        return this.copy(inputEditor = edited)
    }

    // the input group's preferred type is read, as the group's Feature applies it
    fun HomeState.withInputGroupPreferredType(type: DomainColorInputType): HomeState {
        val applied = with(sut.inputGroupCore) { inputGroup.applyPreferredType(type).state }
        return this.copy(inputGroup = applied)
    }
}
