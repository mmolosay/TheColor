package io.github.mmolosay.thecolor.presentation.input.model

/**
 * Result of submitting a data inside the `Color Input`
 * to be processed outside the 'Color Input' feature scope.
 */
data class ColorInputSubmissionResult(
    val wasAccepted: Boolean,
)