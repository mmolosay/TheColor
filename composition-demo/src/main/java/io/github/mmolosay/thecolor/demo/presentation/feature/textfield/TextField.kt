package io.github.mmolosay.thecolor.demo.presentation.feature.textfield

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import io.github.mmolosay.thecolor.demo.R
import androidx.compose.foundation.text.input.TextFieldState as ComposeTextFieldState
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
fun TextField(
    feature: TextFieldFeature,
    label: String,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler?, // null for the key's own action, such as Next moving on
    hasClearButton: Boolean,
    supportingText: String?, // under the field, such as what the field waits for
    outputTransformation: OutputTransformation?, // how the text shows; edits are on the text itself
    modifier: Modifier = Modifier,
) {
    // The text field's internal buffer. The View owns it, so that it can write the state's text into
    // it at once, before the user's next key
    val buffer = remember(feature) {
        ComposeTextFieldState(initialText = feature.viewData.value.text)
    }
    // A text set from elsewhere, such as by the parent, reaches the buffer from a collector, as it
    // reaches the other Views: their collectors run before the next frame is drawn, so the field shows
    // it in the same frame as they do. An edit made on the old text before then is dropped, and the
    // transformation above shows this text
    LaunchedEffect(feature, buffer) {
        feature.viewData.collect {
            buffer.edit { showText(feature.viewData.value.text) } // the newest
        }
    }
    // Each edit of the user's goes to the field as it's made, with the text the buffer held before it
    // as the expected value. The buffer then holds what the field holds: the edit, formatted, or the
    // text from before, if the edit was dropped
    val enterText = remember(feature) {
        InputTransformation {
            val before = originalText.toString()
            val after = asCharSequence().toString()
            if (after != before) {
                feature.enterText(text = after, expected = before)
            }
            showText(feature.viewData.value.text)
        }
    }
    OutlinedTextField(
        state = buffer,
        modifier = modifier,
        label = { Text(text = label) },
        trailingIcon = if (hasClearButton) {
            {
                ClearButton(
                    visible = buffer.text.isNotEmpty(),
                    onClick = {
                        feature.clearText()
                        buffer.edit { showText(feature.viewData.value.text) } // before the next key
                    },
                )
            }
        } else {
            null
        },
        supportingText = supportingText?.let { text ->
            { Text(text = text) }
        },
        inputTransformation = enterText,
        outputTransformation = outputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = TextFieldLineLimits.SingleLine,
    )
}

@Composable
private fun ClearButton(
    visible: Boolean,
    onClick: () -> Unit,
) {
    if (!visible) return
    IconButton(onClick = onClick) {
        Icon(
            imageVector = ImageVector.vectorResource(DesignR.drawable.ic_cross),
            contentDescription = stringResource(R.string.text_field_clear),
        )
    }
}

// the text the state holds, with the cursor kept at the end if it was there
private fun TextFieldBuffer.showText(text: String) {
    if (this.asCharSequence().contentEquals(text)) return
    val wasCursorAtEnd = (this.selection.collapsed && this.selection.end == this.length)
    this.replace(0, this.length, text)
    if (wasCursorAtEnd) this.placeCursorAtEnd()
}
