package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvState
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbState
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text

/**
 * States of the 'Color Input' features, as tests need them.
 * Texts are set as if by code, so that a test can tell them from texts typed by the user.
 */
object MockColorInputStates {

    fun TextFieldData(
        text: String,
        isClearTextFeatureEnabled: Boolean = true,
    ): TextFieldData =
        TextFieldData(
            text = Text(text) causedByUser false,
            shouldSelectAllTextOnFocus = false,
            isClearTextFeatureEnabled = isClearTextFeatureEnabled,
        )

    fun ColorInputHexState(text: String): ColorInputHexState =
        ColorInputHexState(
            textField = TextFieldData(text),
            inputSubmissionResult = null,
        )

    fun ColorInputRgbState(r: String, g: String, b: String): ColorInputRgbState =
        ColorInputRgbState(
            rTextField = TextFieldData(r),
            gTextField = TextFieldData(g),
            bTextField = TextFieldData(b),
            inputSubmissionResult = null,
            isSmartBackspaceEnabled = false,
        )

    /** A state as it is after a [color] was set from code: shown by the pickers and applied. */
    fun ColorInputHsvState(color: Color.Hsv?): ColorInputHsvState =
        ColorInputHsvState(
            displayColor = color,
            color = color,
        )
}
