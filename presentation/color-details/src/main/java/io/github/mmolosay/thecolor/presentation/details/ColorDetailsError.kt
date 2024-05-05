package io.github.mmolosay.thecolor.presentation.details

/**
 * Platform-agnostic data of error provided by ViewModel to color details View.
 */
data class ColorDetailsError(
    val type: Type?,
) {
    enum class Type {
        NoConnection, Timeout, ErrorResponse,
    }
}