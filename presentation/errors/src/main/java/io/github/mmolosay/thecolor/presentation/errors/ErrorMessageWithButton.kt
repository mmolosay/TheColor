package io.github.mmolosay.thecolor.presentation.errors

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.errors.ErrorUiComponents.ActionButton
import io.github.mmolosay.thecolor.presentation.errors.ErrorUiComponents.ErrorLayout
import io.github.mmolosay.thecolor.presentation.errors.ErrorUiComponents.Message

@Composable
fun ErrorMessageWithButton(
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ErrorMessageWithButton(
        modifier = modifier,
        message = message,
        button = {
            ActionButton(
                text = buttonText,
                onClick = onButtonClick,
            )
        },
    )
}

@Composable
fun ErrorMessageWithButton(
    message: String,
    button: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ErrorLayout(
        modifier = modifier,
        message = {
            Message(text = message)
        },
        button = button,
    )
}

@Preview
@Composable
private fun ErrorMessageWithActionPreview() {
    TheColorTheme {
        ErrorMessageWithButton(
            message = "There is no connection to the internet.",
            buttonText = "Try again",
            onButtonClick = {},
        )
    }
}