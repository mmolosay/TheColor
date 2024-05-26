package io.github.mmolosay.thecolor.presentation.errors

import io.github.mmolosay.thecolor.domain.result.HttpFailure
import io.github.mmolosay.thecolor.domain.result.Result

internal fun Result.Failure.toError(): Error {
    val type = this.toErrorTypeOrNull()
    return Error(type = type)
}

private fun Result.Failure.toErrorTypeOrNull(): Error.Type? =
    when (this) {
        is HttpFailure.UnknownHost -> Error.Type.NoConnection
        is HttpFailure.Timeout -> Error.Type.Timeout
        is HttpFailure.ErrorResponse -> Error.Type.ErrorResponse
        else -> null // unsupported or unknown error type
    }