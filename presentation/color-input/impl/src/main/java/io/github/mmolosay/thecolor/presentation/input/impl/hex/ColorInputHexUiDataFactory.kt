package io.github.mmolosay.thecolor.presentation.input.impl.hex

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData

fun ColorInputHexUiData(
    data: ColorInputHexData,
    strings: ColorInputHexUiStrings,
): ColorInputHexUiData =
    ColorInputHexUiData(
        textField = TextFieldUiData(data.textField, strings.textField),
        onImeActionDone = data.submitColor,
    )