package io.github.mmolosay.thecolor.presentation.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.utils.AckValue

/**
 * Reusable UI components for 'Color Input' Views.
 */
internal object UiComponents {

    @Composable
    fun ProcessColorSubmissionResultAsSideEffect(
        ackResult: AckValue<ColorInputSubmissionResult>?,
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(ackResult) {
            if (ackResult == null) return@LaunchedEffect
            try {
                val result = ackResult.value
                // color input was rejected, thus user will probably want to correct it and needs keyboard
                if (result.wasAccepted.not()) return@LaunchedEffect
                // color input was accepted, thus user probably won't change it and doesn't need keyboard
                keyboardController?.hide()
            } finally {
                ackResult.ack()
            }
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