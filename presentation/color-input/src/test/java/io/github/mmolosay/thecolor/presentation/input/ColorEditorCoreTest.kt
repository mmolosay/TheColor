package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorCore
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorOutcome
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexCore
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexOutcome
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldDataFactory
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ColorEditorCoreTest {

    // real implementations, the editor's rule is only meaningful with the colors they produce
    val colorConverter = ColorConverter()
    val colorInputMapper = ColorInputMapper()
    val colorInputValidator = ColorInputValidator(
        colorInputMapper = colorInputMapper,
        colorFactory = ColorFactory(
            colorPrototypeValidator = ColorPrototypeValidator(),
        ),
    )

    val sut = ColorEditorCore(
        hexFactory = ColorInputHexCore.Factory {
            ColorInputHexCore(
                textFieldFactory = TextFieldCore.Factory { formatter ->
                    TextFieldCore(formatter)
                },
                colorConverter = colorConverter,
                colorInputMapper = colorInputMapper,
                colorInputValidator = colorInputValidator,
            )
        },
        colorConverter = colorConverter,
        colorInputMapper = colorInputMapper,
        colorInputValidator = colorInputValidator,
        colorComparator = ColorComparator(colorConverter),
        rgbStateFactory = ColorInputRgbStateFactory(
            textFieldDataFactory = TextFieldDataFactory(),
            colorConverter = colorConverter,
            colorInputMapper = colorInputMapper,
        ),
        hsvStateFactory = ColorInputHsvStateFactory(
            colorConverter = colorConverter,
        ),
    )

    @Test
    fun `given 'hex' holds another color, when 'onHex' is invoked, then 'rgb' and 'hsv' take that color`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF)).withHexText("1A803F")

            val handled = state.onHex(outcome = null)

            val rgbTexts = with(handled.state.rgb) { listOf(rTextField, gTextField, bTextField) }
                .map { it.text.data.string }
            rgbTexts shouldBe listOf("26", "128", "63")
            handled.state.hsv.color shouldBe with(colorConverter) { Color.Hex(0x1A803F).toHsv() }
        }
    }

    @Test
    fun `given 'hex' holds another color, when 'onHex' is invoked, then 'color' is that color`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF)).withHexText("1A803F")

            val handled = state.onHex(outcome = null)

            handled.state.color shouldBe Color.Hex(0x1A803F)
        }
    }

    @Test
    fun `given 'hex' holds another color in a short form, when 'onHex' is invoked, then 'hex' keeps its text`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF)).withHexText("F00")

            val handled = state.onHex(outcome = null)

            handled.state.hex.text() shouldBe "F00"
        }
    }

    @Test
    fun `given 'hex' holds no color, when 'onHex' is invoked, then 'color' is 'null'`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF)).withHexText("1A80") // not a color yet

            val handled = state.onHex(outcome = null)

            handled.state.color shouldBe null
        }
    }

    @Test
    fun `given 'hex' holds the editor's color in another form, when 'onHex' is invoked, then nothing else changes`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFF0000)).withHexText("F00")

            val handled = state.onHex(outcome = null)

            handled shouldBe Change(state)
        }
    }

    @Test
    fun `given 'hex' holds a color and is submitted, when 'onHex' is invoked with 'Submitted', then 'ColorSubmitted' is handed up with that color`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF))
                .withHexText("1A803F")
                .withHexSubmitted()

            val handled = state.onHex(ColorInputHexOutcome.Submitted)

            handled.outcome shouldBe ColorEditorOutcome.ColorSubmitted(Color.Hex(0x1A803F))
        }
    }

    @Test
    fun `given 'hex' holds no color and is submitted, when 'onHex' is invoked with 'Submitted', then 'hex' is answered with 'Rejected', and nothing is handed up`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF))
                .withHexText("1A80") // not a color yet
                .withHexSubmitted()

            val handled = state.onHex(ColorInputHexOutcome.Submitted)

            val submission = handled.state.hex.submission
                .shouldBeInstanceOf<ColorInputSubmission.Answered>()
            submission.answer shouldBe ColorInputSubmission.Answer.Rejected
            handled.outcome.shouldBeNull()
        }
    }

    @Test
    fun `given a gray with 'hsv' at another hue, when 'onHsv' is invoked, then nothing else changes`() {
        with(sut) {
            val state = initialState(Color.Hex(0x808080)).withHsvHue(120f)

            val handled = state.onHsv()

            handled shouldBe Change(state)
        }
    }

    @Test
    fun `given 'hex' holds 'FFF', when 'withColor' is invoked with white, then 'hex' holds 'FFFFFF'`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF)).withHexText("FFF")

            val withWhite = state.withColor(Color.Hex(0xFFFFFF))

            withWhite.hex.text() shouldBe "FFFFFF"
        }
    }

    @Test
    fun `given a gray with 'hsv' at another hue, when 'withColor' is invoked with that gray, then 'hsv' keeps its hue`() {
        with(sut) {
            val gray = Color.Hex(0x808080)
            val state = initialState(gray).withHsvHue(120f)

            val withGray = state.withColor(gray)

            withGray.hsv shouldBe state.hsv
        }
    }

    fun ColorEditorState.withHexText(text: String): ColorEditorState =
        this.copy(hex = MockColorInputStates.ColorInputHexState(text = text))

    fun ColorEditorState.withHexSubmitted(): ColorEditorState {
        val submitted = with(sut.hexCore) { hex.submit() }.state
        return this.copy(hex = submitted)
    }

    fun ColorEditorState.withHsvHue(hue: Float): ColorEditorState {
        val color = this.hsv.color.shouldNotBeNull().copy(hue = hue)
        return this.copy(hsv = MockColorInputStates.ColorInputHsvState(color = color))
    }

    fun ColorInputHexState.text(): String =
        this.textField.text
}
