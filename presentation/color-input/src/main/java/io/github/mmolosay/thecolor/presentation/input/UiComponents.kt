package io.github.mmolosay.thecolor.presentation.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import io.github.mmolosay.thecolor.presentation.common.compose.thenIf
import io.github.mmolosay.thecolor.presentation.input.model.ColorSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

/**
 * Reusable UI components for 'Color Input' Views.
 */
internal object UiComponents {

    @Composable
    fun TextField(
        data: TextFieldData,
        strings: TextFieldUiStrings,
        value: TextFieldValue,
        onValueChange: (TextFieldValue) -> Unit,
        keyboardOptions: KeyboardOptions,
        keyboardActions: KeyboardActions,
        modifier: Modifier = Modifier,
    ) {
        OutlinedTextField(
            modifier = modifier
                .thenIf(data.shouldSelectAllTextOnFocus) {
                    selectAllTextOnFocus(
                        value = value,
                        onValueChange = onValueChange,
                    )
                },
            value = value,
            onValueChange = { new ->
                val current = value
                if (current.text != new.text) {
                    // can't just pass new.text to ViewModel for filtering: TextFieldValue.selection will be lost
                    val filteredText = data.filterUserInput(new.text)
                    val filteredValue = new.copy(text = filteredText.string)
                    onValueChange(filteredValue)
                    data.onTextChange(filteredText)
                } else {
                    onValueChange(new)
                }
            },
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif),
            label = { Label(text = strings.label) },
            placeholder = { Placeholder(text = strings.placeholder) },
            trailingIcon = icon@{
                TrailingButton(
                    feature = data.clearText ?: return@icon,
                    iconContentDesc = strings.trailingIconContentDesc ?: return@icon,
                )
            },
            prefix = if (strings.prefix != null)
                ({ Prefix(text = strings.prefix) })
            else null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
        )
        // for when text is cleared with trailing button or set programmatically
        LaunchedEffect(data.text) {
            @Suppress("UnnecessaryVariable")
            val oldValue = value
            val newText = data.text.data.string
            val newSelection = run {
                val hadSelectionAtTheEnd = (oldValue.selection.end == oldValue.text.length)
                val isNewTextLongerThanOld = (newText.length > oldValue.text.length)
                // if it was "123|" become "123456|" instead of "123|456"
                if (hadSelectionAtTheEnd && isNewTextLongerThanOld) {
                    TextRange(index = newText.length)
                } else {
                    oldValue.selection
                }
            }
            val newValue = oldValue.copy(text = newText, selection = newSelection)
            onValueChange(newValue)
        }
    }

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
    fun ProcessColorSubmissionResultAsSideEffect(
        result: ColorSubmissionResult?,
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(result) {
            result ?: return@LaunchedEffect
            // color input was rejected, thus user will probably want to correct it and needs keyboard
            if (result.wasAccepted.not()) return@LaunchedEffect
            // color input was accepted, thus user probably won't change it and doesn't need keyboard
            keyboardController?.hide()
            result.discard()
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

    @Composable
    private fun Label(text: String) =
        Text(
            text = text,
        )

    @Composable
    private fun Placeholder(text: String) =
        Text(
            text = text,
            color = LocalContentColor.current.copy(alpha = 0.45f),
            style = MaterialTheme.typography.bodyLarge
                .copy(fontFamily = FontFamily.SansSerif),
        )

    @Composable
    private fun TrailingButton(
        feature: TextFieldData.ClearTextFeature,
        iconContentDesc: String,
    ) {
        val updatedFeature by rememberUpdatedState(feature)
        val resizingAlignment = Alignment.Center
        AnimatedVisibility(
            visible = !feature.willBeIdempotent,
            enter = fadeIn() + expandIn(expandFrom = resizingAlignment),
            exit = fadeOut() + shrinkOut(shrinkTowards = resizingAlignment),
        ) {
            IconButton(
                onClick = { updatedFeature.invoke() }, // skip recomposition by creating a lambda that captures the same State object instead of changing feature
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(DesignR.drawable.ic_cross),
                    contentDescription = iconContentDesc,
                )
            }
        }
    }

    @Composable
    private fun Prefix(text: String) =
        Text(
            text = text,
        )

    private fun Modifier.selectAllTextOnFocus(
        value: TextFieldValue,
        onValueChange: (TextFieldValue) -> Unit,
    ) =
        onFocusChanged action@{
            if (!it.isFocused) return@action
            val text = value.text
            val newValue = value.copy(
                selection = TextRange(start = 0, end = text.length)
            )
            onValueChange(newValue)
        }
}