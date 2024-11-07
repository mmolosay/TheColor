package io.github.mmolosay.thecolor.presentation.input.impl.model

/**
 * Result of submitting color to process outside of Color Input scope.
 * Use [discard()][discard] to notify ViewModel that UI has conveyed this event to user and no longer needed.
 */
data class ColorSubmissionResult(
    val wasAccepted: Boolean,
    val discard: () -> Unit,
)