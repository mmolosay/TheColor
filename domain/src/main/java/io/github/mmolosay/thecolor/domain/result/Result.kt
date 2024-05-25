package io.github.mmolosay.thecolor.domain.result

sealed interface Result<out T> {

    data class Success<T>(
        val value: T,
    ) : Result<T>

    sealed interface Failure : Result<Nothing> {
        val cause: Throwable
    }
}

/**
 * Represents a type of [Result.Failure] that occurred during an HTTP call.
 */
sealed class HttpFailure : Result.Failure {

    /** Usually occurs when the device is not connected to a network. */
    class UnknownHost(override val cause: Throwable) : HttpFailure()

    class Timeout(override val cause: Throwable) : HttpFailure()

    class IO(override val cause: Throwable) : HttpFailure()

    class ErrorResponse(
        override val cause: Throwable,
        val code: Int,
        val message: String,
    ) : HttpFailure()
}

fun <T> Result<T>.getOrNull(): T? =
    when (this) {
        is Result.Success -> this.value
        is Result.Failure -> null
    }

fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(this.value)
    }
    return this
}

fun <T> Result<T>.onFailure(action: (Result.Failure) -> Unit): Result<T> {
    if (this is Result.Failure) {
        action(this)
    }
    return this
}