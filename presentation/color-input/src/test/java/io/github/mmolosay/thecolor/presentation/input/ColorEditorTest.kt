package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditor
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorState
import io.github.mmolosay.thecolor.presentation.input.editor.ColorState
import io.github.mmolosay.thecolor.presentation.input.editor.colorState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateLenses
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.reduce
import io.github.mmolosay.thecolor.utils.focus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

/**
 * The conversions and validation are real implementations:
 * the editor's rule is only meaningful together with the colors they produce.
 */
class ColorEditorTest {

    val colorInputMapper = ColorInputMapper()

    val colorConverter = ColorConverter()

    val colorInputValidator = ColorInputValidator(
        colorInputMapper = colorInputMapper,
        colorFactory = ColorFactory(ColorPrototypeValidator()),
    )

    lateinit var sut: ColorEditor

    @Test
    fun `when HEX text that forms a color is set on the text HEX holds, then RGB takes that color`() {
        createSut(hexText = "")

        run {
            val action = TextFieldAction.SetText(text = Text("1A803F"), expected = Text(""))
            sut.hexAtom.focus(ColorInputHexStateLenses.textField).update { it.reduce(action) }
        }

        val rgb = sut.rgbAtom.value
        rgb.rTextField.text.data shouldBe Text("26")
        rgb.gTextField.text.data shouldBe Text("128")
        rgb.bTextField.text.data shouldBe Text("63")
        sut.colorState.source shouldBe DomainColorInputType.Hex
    }

    /**
     * Tests that an edit the user made on the text the View showed is dropped
     * when a color set from code has already replaced that text, so that the color set from code stays.
     */
    @Test
    fun `given a color was set from code, when HEX text made on the text before it arrives, then the color set from code stays`() {
        createSut(hexText = "F0")
        sut.setColor(Color.Hex(0xFFFFFF))

        run {
            val action = TextFieldAction.SetText(text = Text("F0A"), expected = Text("F0"))
            sut.hexAtom.focus(ColorInputHexStateLenses.textField).update { it.reduce(action) }
        }

        sut.hexAtom.value.textField.text.data shouldBe Text("FFFFFF")
        sut.colorState.color shouldBe Color.Hex(0xFFFFFF)
    }

    fun createSut(hexText: String) =
        ColorEditor(
            initialState = ColorEditorState(
                hex = MockColorInputStates.ColorInputHexState(text = hexText),
                rgb = MockColorInputStates.ColorInputRgbState(r = "", g = "", b = ""),
                hsv = MockColorInputStates.ColorInputHsvState(color = null),
                colorState = ColorState(
                    color = null,
                    source = null,
                    revision = ColorState.Revision(0),
                ),
            ),
            colorConverter = colorConverter,
            colorInputMapper = colorInputMapper,
            colorInputValidator = colorInputValidator,
        ).also {
            sut = it
        }
}
