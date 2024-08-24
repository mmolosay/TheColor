package io.github.mmolosay.thecolor.presentation.input.impl.hex

import io.github.mmolosay.thecolor.presentation.input.impl.field.plus
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexUiData.ToggleSoftwareKeyboardCommand.KeyboardState
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexUiData.ViewData

operator fun ColorInputHexData.plus(viewData: ViewData): ColorInputHexUiData =
    ColorInputHexUiData(data = this, viewData = viewData)

private fun ColorInputHexUiData(
    data: ColorInputHexData,
    viewData: ViewData,
): ColorInputHexUiData =
    ColorInputHexUiData(
        textField = data.textField + viewData.textField,
        onImeActionDone = data.submitColor,
        toggleSoftwareKeyboardCommand = ToggleSoftwareKeyboardCommand(data),
    )

private fun ToggleSoftwareKeyboardCommand(
    data: ColorInputHexData,
): ColorInputHexUiData.ToggleSoftwareKeyboardCommand? {
    val result = data.colorSubmissionResult
    if (result == null) return null
    val destState = when (result.wasAccepted) {
        true -> KeyboardState.Hidden // color input was accepted, thus user probably won't change it
        false -> KeyboardState.Visible // color input was rejected, thus user will probably want to correct it
    }
    return ColorInputHexUiData.ToggleSoftwareKeyboardCommand(
        destState = destState,
        onExecuted = result.discard,
    )
}