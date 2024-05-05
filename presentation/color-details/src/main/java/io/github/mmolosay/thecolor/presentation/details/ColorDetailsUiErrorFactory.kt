package io.github.mmolosay.thecolor.presentation.details

internal fun ColorDetailsUiError(
    error: ColorDetailsError,
    viewData: ColorDetailsUiData.ViewData,
): ColorDetailsUiError =
    ColorDetailsUiError(
        text = error.type.toUiText(viewData),
    )

private fun ColorDetailsError.Type?.toUiText(
    viewData: ColorDetailsUiData.ViewData,
): String =
    when (this) {
        ColorDetailsError.Type.NoConnection -> viewData.errorMessageNoConnection
        ColorDetailsError.Type.Timeout -> viewData.errorMessageTimeout
        ColorDetailsError.Type.ErrorResponse -> viewData.errorMessageErrorResponse
        null -> viewData.errorMessageUnexpectedError
    }