package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

/**
 * Platform-agnostic data about error provided by ViewModel to 'Color Scheme' View.
 */
data class ColorSchemeError(
    val cause: Throwable,
    val tryAgain: () -> Unit,
)