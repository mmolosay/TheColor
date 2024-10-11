package io.github.mmolosay.thecolor.presentation.input.impl

fun ColorInputUiData(
    data: ColorInputData,
    strings: ColorInputUiStrings,
): ColorInputUiData =
    ColorInputUiData(
        selectedViewType = data.selectedViewType,
        orderedViewTypes = data.orderedViewTypes,
        onInputTypeChange = data.onInputTypeChange,
        hexLabel = strings.hexLabel,
        rgbLabel = strings.rgbLabel,
    )