package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.CancellationException

inline fun <T> Result<T>.mapFailure(
    transform: (Throwable) -> Throwable,
): Result<T> {
    val initialException = this.exceptionOrNull() ?: return this
    val mappedException = transform(initialException)
    return Result.failure(mappedException)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Result<T>.rethrowCancellationException(): Result<T> =
    onFailure {
        if (it is CancellationException) throw it
    }