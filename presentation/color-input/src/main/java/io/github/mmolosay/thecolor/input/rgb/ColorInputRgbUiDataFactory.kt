package io.github.mmolosay.thecolor.input.rgb

import io.github.mmolosay.thecolor.input.field.plus
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbUiData.ViewData

operator fun ColorInputRgbData.plus(viewData: ViewData): ColorInputRgbUiData =
    combine(data = this, viewData = viewData)

private fun combine(
    data: ColorInputRgbData,
    viewData: ViewData,
): ColorInputRgbUiData =
    ColorInputRgbUiData(
        rTextField = data.rTextField + viewData.rTextField,
        gTextField = data.gTextField + viewData.gTextField,
        bTextField = data.bTextField + viewData.bTextField,
    )