package io.github.mmolosay.thecolor.presentation.details.viewmodel

/**
 * Platform-agnostic data about error provided by ViewModel to 'Color Details' View.
 */
data class ColorDetailsError(
    val cause: Throwable,
    val tryAgain: () -> Unit,
)