package io.github.mmolosay.thecolor.domain.failure

/**
 * Represents a type of [Failure] that occurred during an HTTP call.
 */
sealed class HttpFailure : Failure {

    class UnknownHost(override val cause: Throwable) : HttpFailure()
    class Timeout(override val cause: Throwable) : HttpFailure()
    class IO(override val cause: Throwable) : HttpFailure()
    class ErrorResponse(
        override val cause: Throwable,
        val code: Int,
        val message: String,
    ) : HttpFailure()
}