package io.github.mmolosay.thecolor.presentation.errors

/**
 * Platform-agnostic data of error.
 * The [type] of error is defined by an instance of domain [Result.Failure][io.github.mmolosay.thecolor.domain.result.Result.Failure].
 */
data class Error(
    val type: Type?,
) {
    enum class Type {
        NoConnection,
        Timeout,
        ErrorResponse,
    }
}