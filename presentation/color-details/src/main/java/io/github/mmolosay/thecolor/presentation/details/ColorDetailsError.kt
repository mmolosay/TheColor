package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.presentation.errors.ErrorType

/**
 * Platform-agnostic data about error provided by ViewModel to color details View.
 */
data class ColorDetailsError(
    val type: ErrorType,
    val action: () -> Unit,
)