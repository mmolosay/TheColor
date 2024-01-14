package io.github.mmolosay.presentation.util.ext

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import java.lang.RuntimeException

fun <V> Result.Companion.success(value: V): Result<V, Nothing> {
    return Ok(value)
}

fun <E> Result.Companion.error(error: E): Result<Nothing, E> {
    return Err(error)
}

fun Result.Companion.error(errorMsg: String): Result<Nothing, RuntimeException> {
    return Err(RuntimeException(errorMsg))
}

inline fun <V> V?.toResultOrError(errorMsg: () -> String): Result<V, RuntimeException> {
    return when (this) {
        null -> Result.error(errorMsg())
        else -> Result.success(this)
    }
}