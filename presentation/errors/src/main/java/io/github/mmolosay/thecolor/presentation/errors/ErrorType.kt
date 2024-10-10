package io.github.mmolosay.thecolor.presentation.errors

import io.github.mmolosay.thecolor.domain.result.HttpFailure
import io.github.mmolosay.thecolor.domain.result.Result

/**
 * Presentational type of an error.
 * Usually defined by an instance of domain [Result.Failure][io.github.mmolosay.thecolor.domain.result.Result.Failure].
 */
enum class ErrorType {
    NoConnection,
    Timeout,
    ErrorResponse,
    Unknown,
}

fun Result.Failure.toErrorType(): ErrorType =
    when (this) {
        is HttpFailure.UnknownHost -> ErrorType.NoConnection
        is HttpFailure.Timeout -> ErrorType.Timeout
        is HttpFailure.ErrorResponse -> ErrorType.ErrorResponse
        else -> ErrorType.Unknown // unsupported or unknown error type
    }

fun ErrorType.message(
    viewData: ErrorViewData,
): String =
    when (this) {
        ErrorType.NoConnection -> viewData.messageNoConnection
        ErrorType.Timeout -> viewData.messageTimeout
        ErrorType.ErrorResponse -> viewData.messageErrorResponse
        ErrorType.Unknown -> viewData.messageUnexpectedError
    }