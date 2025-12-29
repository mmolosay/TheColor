package io.github.mmolosay.thecolor.presentation.input.picker

import io.github.mmolosay.thecolor.domain.model.Color

/**
 * Platform-agnostic data provided by ViewModel to 'Visual Picker Color Input' View.
 */
data class ColorInputPickerData(
    val onColorChanged: (Color) -> Unit,
)