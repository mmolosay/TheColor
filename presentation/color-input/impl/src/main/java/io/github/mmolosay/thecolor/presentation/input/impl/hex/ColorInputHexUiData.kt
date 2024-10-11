package io.github.mmolosay.thecolor.presentation.input.impl.hex

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData

/**
 * Framework-oriented data required for HEX color input View to be presented by Compose.
 */
data class ColorInputHexUiData(
    val textField: TextFieldUiData,
    val onImeActionDone: () -> Unit,
)