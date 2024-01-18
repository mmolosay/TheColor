package io.github.mmolosay.thecolor.presentation.home.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.TrailingButton

/**
 * Reusable UI components for color input Views.
 */
object ColorInputComponents {

    @Composable
    fun TextField(
        modifier: Modifier = Modifier,
        uiData: ColorInputFieldUiData,
        value: TextFieldValue,
        updateValue: (TextFieldValue) -> Unit,
        keyboardOptions: KeyboardOptions,
    ) =
        with(uiData) {
            OutlinedTextField(
                modifier = modifier
                    .selectAllTextOnFocus(
                        value = value,
                        onValueChange = updateValue,
                    ),
                value = value,
                onValueChange = { new ->
                    val filteredText = filterUserInput(new.text)
                    val filteredValue = new.copy(text = filteredText.string)
                    updateValue(filteredValue)
                    onTextChange(filteredText)
                },
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif),
                label = { Label(label) },
                placeholder = { Placeholder(placeholder) },
                trailingIcon = { TrailingButton(trailingButton) },
                prefix = if (prefix != null) ({ Prefix(prefix) }) else null,
                keyboardOptions = keyboardOptions,
                keyboardActions = KeyboardActions(),
                singleLine = true,
            )
            // for when text is cleared with trailing button
            LaunchedEffect(text) {
                val new = value.copy(text = text.string)
                updateValue(new)
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
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        )

    @Composable
    private fun TrailingButton(uiData: TrailingButton) {
        val resizingAlignment = Alignment.Center
        // when uiData is Hidden, we want to have memoized Visible data for some time while "exit" animation is running
        var visibleUiData by remember { mutableStateOf<TrailingButton.Visible?>(null) }
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
        onFocusChanged a@{
            if (!it.isFocused) return@a
            val text = value.text
            val newValue = value.copy(
                selection = TextRange(start = 0, end = text.length)
            )
            onValueChange(newValue)
        }
}