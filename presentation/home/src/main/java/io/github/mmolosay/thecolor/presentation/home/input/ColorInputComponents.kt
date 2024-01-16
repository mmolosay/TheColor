package io.github.mmolosay.thecolor.presentation.home.input

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Reusable UI components of color input widgets.
 */
internal object ColorInputComponents {

    @Composable
    fun TextField(
        modifier: Modifier = Modifier,
        uiData: ColorInputFieldUiData,
        value: TextFieldValue,
        updateValue: (TextFieldValue) -> Unit,
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
                    val processed = new.copy(text = processText(new.text))
                    updateValue(processed)
                    onTextChange(new.text)
                },
                label = { Label(label) },
                placeholder = { Placeholder(placeholder) },
                trailingIcon = { TrailingButton(trailingButton) },
                prefix = { Prefix(prefix) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(),
                singleLine = true,
            )
            // for when text is cleared with trailing button
            LaunchedEffect(text) {
                val new = value.copy(text = text)
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
        )

    @Composable
    private fun TrailingButton(uiData: ColorInputFieldUiData.TrailingButton) =
        when (uiData) {
            is ColorInputFieldUiData.TrailingButton.Visible -> ClearIconButton(uiData)
            is ColorInputFieldUiData.TrailingButton.Hidden -> Unit // don't show anything
        }

    @Composable
    private fun ClearIconButton(uiData: ColorInputFieldUiData.TrailingButton.Visible) {
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