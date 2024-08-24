package io.github.mmolosay.thecolor.presentation.input.impl.hex

import io.github.mmolosay.thecolor.presentation.input.impl.field.plus
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
    )