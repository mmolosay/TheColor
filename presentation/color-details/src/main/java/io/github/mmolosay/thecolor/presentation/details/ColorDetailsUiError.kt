package io.github.mmolosay.thecolor.presentation.details

/**
 * Framework-oriented data required for error on color details View to be presented by Compose.
 * It is a combination of [ColorDetailsError] and [ColorDetailsUiData.ViewData].
 */
data class ColorDetailsUiError(
    val text: String,
)