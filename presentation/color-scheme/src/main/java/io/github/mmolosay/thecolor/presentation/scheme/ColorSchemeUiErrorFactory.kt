package io.github.mmolosay.thecolor.presentation.scheme

internal fun ColorSchemeUiError(
    error: ColorSchemeError,
    viewData: ColorSchemeUiData.ViewData,
): ColorSchemeUiError =
    ColorSchemeUiError(
        text = error.type.toUiText(viewData),
    )

private fun ColorSchemeError.Type?.toUiText(
    viewData: ColorSchemeUiData.ViewData,
): String =
    when (this) {
        ColorSchemeError.Type.NoConnection -> viewData.errorMessageNoConnection
        ColorSchemeError.Type.Timeout -> viewData.errorMessageTimeout
        ColorSchemeError.Type.ErrorResponse -> viewData.errorMessageErrorResponse
        null -> viewData.errorMessageUnexpectedError
    }