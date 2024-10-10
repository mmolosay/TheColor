package io.github.mmolosay.thecolor.presentation.settings

/**
 * Platform-agnostic data provided by ViewModel to settings View.
 */
data class SettingsData(
    val preferredColorInput: ColorInputType,
    val changePreferredColorInput: (ColorInputType) -> Unit,
) {

    enum class ColorInputType {
        Hex, Rgb,
    }
}