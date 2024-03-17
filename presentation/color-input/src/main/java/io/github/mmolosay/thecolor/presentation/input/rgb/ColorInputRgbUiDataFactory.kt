package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.field.plus
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbUiData.ViewData

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
    )