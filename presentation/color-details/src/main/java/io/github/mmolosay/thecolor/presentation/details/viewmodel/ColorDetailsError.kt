package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color

/**
 * Platform-agnostic data about an error that occurred in 'Color Details' feature.
 *
 * @param cause the error itself, provided by ViewModel to 'Color Details' View.
 * @param origin the operation that has failed. Not consumed by the View: it is what the ViewModel
 * repeats when [ColorDetailsAction.RetryOnError] is executed.
 */
data class ColorDetailsError(
    val cause: Throwable,
    val origin: Origin,
) {

    /** An operation that produced a [ColorDetailsError]. */
    sealed interface Origin {
        data class SetSeedColor(val color: Color) : Origin
        data class SelectColor(val role: ColorRole) : Origin
    }
}