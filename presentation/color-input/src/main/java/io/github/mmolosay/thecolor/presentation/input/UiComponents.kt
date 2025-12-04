package io.github.mmolosay.thecolor.presentation.input

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.github.mmolosay.thecolor.presentation.input.model.ColorSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import kotlinx.coroutines.flow.StateFlow

/**
 * Reusable UI components for 'Color Input' Views.
 */
internal object UiComponents {

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun <T> DataStateCrossfade(
        actualDataState: DataState<T>,
        content: @Composable (targetState: DataState<T>) -> Unit,
    ) {
        val transition = updateTransition(
            targetState = actualDataState,
            label = "data state cross-fade",
        )
        val animationSpec = tween<Float>(
            durationMillis = 500,
            easing = FastOutSlowInEasing,
        )
        transition.Crossfade(
            animationSpec = animationSpec,
            contentKey = { it::class }, // don't animate when 'DataState' type stays the same,
            content = content,
        )
    }

    @Composable
    fun CollectColorSubmissionResultAsSideEffect(
        resultFlow: StateFlow<ColorSubmissionResult?>,
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(resultFlow) {
            resultFlow.collect { result ->
                if (result == null) return@collect
                // color input was rejected, thus user will probably want to correct it and needs keyboard
                if (result.wasAccepted.not()) return@collect
                // color input was accepted, thus user probably won't change it and doesn't need keyboard
                keyboardController?.hide()
                result.discard()
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