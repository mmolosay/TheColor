package io.github.mmolosay.thecolor.presentation.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.errors.ErrorUiComponents.ErrorLayout
import io.github.mmolosay.thecolor.presentation.errors.ErrorUiComponents.Message

@Composable
fun ErrorMessage(
    errorType: ErrorType,
    strings: ErrorsUiStrings = rememberDefaultErrorsUiStrings(),
    modifier: Modifier = Modifier,
) {
    val message = errorType.message(strings)
    ErrorMessage(
        text = message,
        modifier = modifier,
    )
}

@Composable
fun ErrorMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    ErrorLayout(
        modifier = modifier,
        message = {
            Message(text = text)
        },
        button = null
    )
}

@Preview
@Composable
private fun ErrorMessagePreview() {
    TheColorTheme {
        ErrorMessage(
            text = "There is no connection to the internet.",
        )
    }
}