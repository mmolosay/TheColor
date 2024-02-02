package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.presentation.input.ColorInputUiData.ViewData

operator fun ColorInputData.plus(viewData: ViewData): ColorInputUiData =
    ColorInputUiData(data = this, viewData = viewData)

private fun ColorInputUiData(
    data: ColorInputData,
    viewData: ViewData,
): ColorInputUiData =
    ColorInputUiData(
        viewType = data.viewType,
        onInputTypeChange = data.onInputTypeChange,
        hexLabel = viewData.hexLabel,
        rgbLabel = viewData.rgbLabel,
    )