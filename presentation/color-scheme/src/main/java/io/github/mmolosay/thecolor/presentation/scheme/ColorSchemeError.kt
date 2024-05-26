package io.github.mmolosay.thecolor.presentation.scheme

/**
 * Platform-agnostic data of error provided by ViewModel to color scheme View.
 */
// TODO: Similar as 'ColorDetailsError'; reuse from shared presentation module?
data class ColorSchemeError(
    val type: Type?,
) {
    enum class Type {
        NoConnection, Timeout, ErrorResponse,
    }
}