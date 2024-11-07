package io.github.mmolosay.thecolor.presentation.errors

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ErrorsUiStrings(
    val messageNoConnection: String,
    val messageTimeout: String,
    val messageErrorResponse: String,
    val messageUnexpectedError: String,
    val actionTryAgain: String,
)

fun ErrorsUiStrings(context: Context) =
    ErrorsUiStrings(
        messageNoConnection = context.getString(R.string.errors_message_no_connection),
        messageTimeout = context.getString(R.string.errors_message_timeout),
        messageErrorResponse = context.getString(R.string.errors_message_any_error_response),
        messageUnexpectedError = context.getString(R.string.errors_message_unexpected_error),
        actionTryAgain = context.getString(R.string.error_action_try_again)
    )

@Composable
fun rememberDefaultErrorsUiStrings(): ErrorsUiStrings {
    val context = LocalContext.current
    return remember(context) { ErrorsUiStrings(context) }
}