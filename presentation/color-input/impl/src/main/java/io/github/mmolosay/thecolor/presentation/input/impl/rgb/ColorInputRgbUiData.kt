package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData

/**
 * Framework-oriented data required for RGB color input View to be presented by Compose.
 */
data class ColorInputRgbUiData(
    val rTextField: TextFieldUiData,
    val gTextField: TextFieldUiData,
    val bTextField: TextFieldUiData,
    val onImeActionDone: () -> Unit,
    val addSmartBackspaceModifier: Boolean, // TODO: used in TextField() Composables. Create specific RgbTextFieldUiData with such property?
)