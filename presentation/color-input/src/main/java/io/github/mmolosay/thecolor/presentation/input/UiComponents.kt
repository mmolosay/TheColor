package io.github.mmolosay.thecolor.presentation.input

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission

/**
 * Reusable UI components for 'Color Input' Views.
 */
internal object UiComponents {

    @Composable
    fun ProcessSubmissionAnswerAsSideEffect(
        submission: ColorInputSubmission.Answered?,
        rejectionMessage: String,
        onProcessed: (ColorInputSubmission.Answered) -> Unit,
    ) {
        val context = LocalContext.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val onProcessed by rememberUpdatedState(onProcessed) // intended name shadowing to enforce usage of latest lambda
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
            onProcessed(submission)
        }
    }
}