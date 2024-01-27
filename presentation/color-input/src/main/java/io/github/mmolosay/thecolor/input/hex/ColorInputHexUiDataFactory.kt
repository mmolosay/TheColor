package io.github.mmolosay.thecolor.input.hex

import io.github.mmolosay.thecolor.input.field.plus
import io.github.mmolosay.thecolor.input.hex.ColorInputHexUiData.ViewData

operator fun ColorInputHexData.plus(viewData: ViewData): ColorInputHexUiData =
    ColorInputHexUiData(data = this, viewData = viewData)

private fun ColorInputHexUiData(
    data: ColorInputHexData,
    viewData: ViewData,
): ColorInputHexUiData =
    ColorInputHexUiData(
        textField = data.textField + viewData.textField,
    )