package io.github.mmolosay.thecolor.presentation.input.impl

fun ColorInputUiData(
    data: ColorInputData,
    strings: ColorInputUiStrings,
): ColorInputUiData =
    ColorInputUiData(
        selectedInputType = data.selectedInputType,
        orderedInputTypes = data.orderedInputTypes,
        onInputTypeChange = data.onInputTypeChange,
        hexLabel = strings.hexLabel,
        rgbLabel = strings.rgbLabel,
    )