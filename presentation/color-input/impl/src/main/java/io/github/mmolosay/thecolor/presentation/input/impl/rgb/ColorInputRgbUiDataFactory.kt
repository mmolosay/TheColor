package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import io.github.mmolosay.thecolor.presentation.input.impl.field.plus
import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorInputUiCommand
import io.github.mmolosay.thecolor.presentation.input.impl.rgb.ColorInputRgbUiData.ViewData

operator fun ColorInputRgbData.plus(viewData: ViewData): ColorInputRgbUiData =
    ColorInputRgbUiData(data = this, viewData = viewData)

private fun ColorInputRgbUiData(
    data: ColorInputRgbData,
    viewData: ViewData,
): ColorInputRgbUiData =
    ColorInputRgbUiData(
        rTextField = data.rTextField + viewData.rTextField,
        gTextField = data.gTextField + viewData.gTextField,
        bTextField = data.bTextField + viewData.bTextField,
        onImeActionDone = data.submitColor,
        hideSoftwareKeyboardCommand = hideSoftwareKeyboardCommandOrNull(data),
    )

private fun hideSoftwareKeyboardCommandOrNull(
    data: ColorInputRgbData,
): ColorInputUiCommand.HideSoftwareKeyboard? {
    val result = data.colorSubmissionResult ?: return null
    // color input was rejected, thus user will probably want to correct it and needs keyboard
    if (result.wasAccepted.not()) return null
    // color input was accepted, thus user probably won't change it and doesn't need keyboard
    return ColorInputUiCommand.HideSoftwareKeyboard(onExecuted = result.discard)
}