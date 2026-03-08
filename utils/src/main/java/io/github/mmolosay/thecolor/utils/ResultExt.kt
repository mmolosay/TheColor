package io.github.mmolosay.thecolor.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T> Result<T>.mapFailure(
    transform: (Throwable) -> Throwable,
): Result<T> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    val exception = this.exceptionOrNull()
    return when (exception) {
        null -> this
        else -> Result.failure(transform(exception))
    }
}