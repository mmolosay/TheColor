package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.reduce
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Tests [TextFieldData.reduce] extension.
 */
class TextFieldDataExtReduceTest {

    @Test
    fun `when 'SetText' expects the text the field holds, then the new text is applied as typed by the user`() {
        val data = MockColorInputStates.TextFieldData(text = "F0")

        val newData = data.reduce(TextFieldAction.SetText(text = Text("F00"), expected = Text("F0")))

        newData.text shouldBe (Text("F00") causedByUser true)
    }

    @Test
    fun `when 'SetText' expects a text the field no longer holds, then the data is NOT changed`() {
        val data = MockColorInputStates.TextFieldData(text = "FFFFFF")

        val newData = data.reduce(TextFieldAction.SetText(text = Text("F0A"), expected = Text("F0")))

        newData shouldBe data
    }

    /**
     * Tests that a new text is applied whenever the field holds the text it was made from,
     * even if the field held another text in between: the new text depends on nothing else,
     * so it is just as right as if the user typed it now.
     */
    @Test
    fun `given the text changed and changed back, when 'SetText' expects that text, then the new text is applied`() {
        val data = MockColorInputStates.TextFieldData(text = "")
            .reduce(TextFieldAction.SetText(text = Text("FFFFFF"), expected = Text("")))
            .reduce(TextFieldAction.SetText(text = Text(""), expected = Text("FFFFFF")))

        val newData = data.reduce(TextFieldAction.SetText(text = Text("A"), expected = Text("")))

        newData.text.data shouldBe Text("A")
    }

    @Test
    fun `given 'clear text' feature is enabled, when it is invoked, then the text is cleared as by the user`() {
        val data = MockColorInputStates.TextFieldData(
            text = "1A803F",
            isClearTextFeatureEnabled = true,
        )

        val newData = data.reduce(TextFieldAction.ClearTextFeature.Invoke)

        newData.text shouldBe (Text("") causedByUser true)
    }

    @Test
    fun `given 'clear text' feature is disabled, when it is invoked, then the data is NOT changed`() {
        val data = MockColorInputStates.TextFieldData(
            text = "1A803F",
            isClearTextFeatureEnabled = false,
        )

        val newData = data.reduce(TextFieldAction.ClearTextFeature.Invoke)

        newData shouldBe data
    }
}
