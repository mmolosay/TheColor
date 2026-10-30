package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorCore
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldDataFactory
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ColorEditorCoreTest {

    // real implementations, the editor's rule is only meaningful with the colors they produce
    val colorConverter = ColorConverter()
    val colorInputMapper = ColorInputMapper()
    val textFieldDataFactory = TextFieldDataFactory()

    val sut = ColorEditorCore(
        colorConverter = colorConverter,
        colorInputMapper = colorInputMapper,
        colorInputValidator = ColorInputValidator(
            colorInputMapper = colorInputMapper,
            colorFactory = ColorFactory(ColorPrototypeValidator()),
        ),
        colorComparator = ColorComparator(colorConverter),
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

    @Test
    fun `given 'hex' holds another color, when 'onHex' is invoked, then 'rgb' and 'hsv' take that color`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF)).withHexText("1A803F")

            val handled = state.onHex()

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

            val handled = state.onHex()

            handled.state.color shouldBe Color.Hex(0x1A803F)
        }
    }

    @Test
    fun `given 'hex' holds another color in a short form, when 'onHex' is invoked, then 'hex' keeps its text`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF)).withHexText("F00")

            val handled = state.onHex()

            handled.state.hex.text() shouldBe "F00"
        }
    }

    @Test
    fun `given 'hex' holds no color, when 'onHex' is invoked, then 'color' is 'null'`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFFFFFF)).withHexText("1A80") // not a color yet

            val handled = state.onHex()

            handled.state.color shouldBe null
        }
    }

    @Test
    fun `given 'hex' holds the editor's color in another form, when 'onHex' is invoked, then nothing else changes`() {
        with(sut) {
            val state = initialState(Color.Hex(0xFF0000)).withHexText("F00")

            val handled = state.onHex()

            handled shouldBe Change(state)
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

    // only the text of 'hex' changes
    fun ColorEditorState.withHexText(text: String): ColorEditorState =
        this.copy(hex = MockColorInputStates.ColorInputHexState(text = text))

    // 'hsv' at another hue of the same gray
    fun ColorEditorState.withHsvHue(hue: Float): ColorEditorState {
        val color = this.hsv.color.shouldNotBeNull().copy(hue = hue)
        return this.copy(hsv = MockColorInputStates.ColorInputHsvState(color = color))
    }

    fun ColorInputHexState.text(): String =
        this.textField.text.data.string
}
