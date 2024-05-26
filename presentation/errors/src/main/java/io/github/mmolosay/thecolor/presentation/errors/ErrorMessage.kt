package io.github.mmolosay.thecolor.presentation.errors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@Composable
fun ErrorMessage(
    failure: Result.Failure,
    viewData: ErrorViewData = defaultErrorViewData(),
    modifier: Modifier = Modifier,
) {
    val error = remember(failure) { failure.toError() }
    val text = error.errorMessage(viewData)
    ErrorMessage(
        text = text,
        modifier = modifier,
    )
}

@Composable
fun ErrorMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 200.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
        )
    }
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

internal fun Error.errorMessage(
    viewData: ErrorViewData,
): String =
    when (this.type) {
        Error.Type.NoConnection -> viewData.messageNoConnection
        Error.Type.Timeout -> viewData.messageTimeout
        Error.Type.ErrorResponse -> viewData.messageErrorResponse
        null -> viewData.messageUnexpectedError
    }