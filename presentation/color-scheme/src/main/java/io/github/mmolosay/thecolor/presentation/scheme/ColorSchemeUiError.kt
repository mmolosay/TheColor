package io.github.mmolosay.thecolor.presentation.scheme

/**
 * Framework-oriented data required for error on color details View to be presented by Compose.
 * It is a combination of [ColorSchemeError] and [ColorSchemeUiData.ViewData].
 */
data class ColorSchemeUiError(
    val text: String,
)