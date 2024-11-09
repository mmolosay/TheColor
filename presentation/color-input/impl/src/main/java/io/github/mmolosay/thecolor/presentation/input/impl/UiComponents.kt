package io.github.mmolosay.thecolor.presentation.input.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData.TrailingButton

/**
 * Reusable UI components for color input Views.
 */
internal object UiComponents {

    @Composable
    fun Loading() =
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        )

    @Composable
    fun TextField(
        modifier: Modifier = Modifier,
        uiData: TextFieldUiData,
        value: TextFieldValue,
        onValueChange: (TextFieldValue) -> Unit,
        keyboardOptions: KeyboardOptions,
        keyboardActions: KeyboardActions,
    ) =
        with(uiData) {
            OutlinedTextField(
                modifier = modifier
                    .selectAllTextOnFocus(
                        value = value,
                        onValueChange = onValueChange,
                    ),
                value = value,
                onValueChange = { new ->
                    // can't just pass new.text to ViewModel for filtering: TextFieldValue.selection will be lost
                    val filteredText = filterUserInput(new.text)
                    val filteredValue = new.copy(text = filteredText.string)
                    onValueChange(filteredValue)
                    onTextChange(filteredText)
                },
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif),
                label = { Label(label) },
                placeholder = { Placeholder(placeholder) },
                trailingIcon = { TrailingButton(trailingButton) },
                prefix = if (prefix != null) ({ Prefix(prefix) }) else null,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = true,
            )
            // for when text is cleared with trailing button or set programmatically
            LaunchedEffect(text) {
                val old = value
                val newText = text.string
                val hadSelectionAtTheEnd = (old.selection.end == old.text.length)
                val isNewTextLongerThanOld = (newText.length > old.text.length)
                // if it was "123|" become "123456|" instead of "123|456"
                val newSelection = if (hadSelectionAtTheEnd && isNewTextLongerThanOld) {
                    TextRange(index = newText.length)
                } else {
                    old.selection
                }
                val new = old.copy(text = newText, selection = newSelection)
                onValueChange(new)
            }
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
    private fun TrailingButton(uiData: TrailingButton) {
        // when uiData is Hidden, we want to have memoized Visible data for some time while "exit" animation is running
        var visibleUiData by remember { mutableStateOf<TrailingButton.Visible?>(null) }
        val resizingAlignment = Alignment.Center
        AnimatedVisibility(
            visible = uiData is TrailingButton.Visible,
            enter = fadeIn() + expandIn(expandFrom = resizingAlignment),
            exit = fadeOut() + shrinkOut(shrinkTowards = resizingAlignment),
        ) {
            val lastVisible = visibleUiData ?: return@AnimatedVisibility
            ClearIconButton(lastVisible)
        }
        LaunchedEffect(uiData) {
            visibleUiData = uiData as? TrailingButton.Visible ?: return@LaunchedEffect
        }
    }

    @Composable
    private fun ClearIconButton(uiData: TrailingButton.Visible) {
        IconButton(
            onClick = uiData.onClick,
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = uiData.iconContentDesc,
            )
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