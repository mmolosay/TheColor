package io.github.mmolosay.thecolor.presentation.input

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult

/**
 * Reusable UI components for 'Color Input' Views.
 */
internal object UiComponents {

    // TODO: remove when no longer used
    @Composable
    fun ProcessColorSubmissionResultAsSideEffect(
        result: ColorInputSubmissionResult?,
        ack: () -> Unit,
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val ack by rememberUpdatedState(ack) // intended name shadowing to enforce usage of latest lambda
        LaunchedEffect(result) {
            if (result == null) return@LaunchedEffect
            try {
                // color input was rejected, thus user will probably want to correct it and needs keyboard
                if (result.wasAccepted.not()) return@LaunchedEffect
                // color input was accepted, thus user probably won't change it and doesn't need keyboard
                keyboardController?.hide()
            } finally {
                ack()
            }
        }
    }

    @Composable
    fun ProceedSubmissionAnswerAsSideEffect(
        submission: ColorInputSubmission.Answered?,
        rejectionMessage: String,
        onShown: (ColorInputSubmission.Answered) -> Unit,
    ) {
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val onShown by rememberUpdatedState(onShown) // intended name shadowing to enforce usage of latest lambda
        if (submission == null) return
        LaunchedEffect(submission) {
            when (submission.answer) {
                ColorInputSubmission.Answer.Accepted -> {
                    // color input was accepted, thus user probably won't change it and doesn't need keyboard
                    keyboardController?.hide()
                }
                ColorInputSubmission.Answer.Rejected -> {
                    // color input was rejected, thus user will probably want to correct it and needs keyboard
                    Toast.makeText(context, rejectionMessage, Toast.LENGTH_SHORT).show()
                }
            }
            onShown(submission)
        }
    }

    fun Modifier.onBackspace(
        onBackspace: () -> Unit,
    ) = this.onKeyEvent { keyEvent ->
        if (keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyUp) {
            onBackspace()
            return@onKeyEvent true
        }
        return@onKeyEvent false
    }
}