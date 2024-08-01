package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.presentation.errors.ErrorType

/**
 * Platform-agnostic data about error provided by ViewModel to color scheme View.
 */
data class ColorSchemeError(
    val type: ErrorType,
    val action: () -> Unit,
)