package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData

fun ColorInputRgbUiData(
    data: ColorInputRgbData,
    strings: ColorInputRgbUiStrings,
): ColorInputRgbUiData =
    ColorInputRgbUiData(
        rTextField = TextFieldUiData(data.rTextField, strings.rTextField),
        gTextField = TextFieldUiData(data.gTextField, strings.gTextField),
        bTextField = TextFieldUiData(data.bTextField, strings.bTextField),
        onImeActionDone = data.submitColor,
        addSmartBackspaceModifier = data.isSmartBackspaceEnabled,
    )