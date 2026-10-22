package io.github.mmolosay.thecolor.demo.presentation.feature.textfield

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.github.mmolosay.thecolor.demo.R
import io.github.mmolosay.thecolor.presentation.common.composition.collectAsViewData
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

/**
 * An edit made on text that [field] no longer holds is dropped, and the field shows what it holds
 * instead.
 *
 * The other parameters depend on where the field sits rather than on its state, so the parent's
 * View sets them.
 */
@Composable
fun TextField(
    field: TextFieldFeature,
    label: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    hasClearButton: Boolean,
    modifier: Modifier = Modifier,
) {
    val viewData by field.viewData.collectAsViewData()
    val text = viewData.text
    // The control applies each edit to its own buffer and has to get the edit back in the same
    // callback, so the View keeps a copy of what it shows, with the cursor the state doesn't hold.
    // Every edit recomposes, even one equal to the last: a repeated rejected edit must still reset
    // the latest value below to what the control shows.
    val copy = remember(field) {
        val value = TextFieldValue(text = text, selection = TextRange(text.length))
        mutableStateOf(value, neverEqualPolicy())
    }
    // The control's latest value: the result of its last edit, or what the View passed to it since,
    // whichever came later. The next edit is built on it, so it's that edit's expected value.
    val latest = remember(field) { mutableStateOf(copy.value) }
    val shown = copy.value.withText(text)
    SideEffect {
        latest.value = shown
    }
    OutlinedTextField(
        value = shown,
        onValueChange = { new ->
            val old = latest.value
            latest.value = new
            copy.value = new
            if (new.text != old.text) {
                field.enterText(text = new.text, expected = old.text)
            }
        },
        modifier = modifier,
        label = { Text(text = label) },
        trailingIcon = if (hasClearButton) {
            {
                ClearButton(
                    visible = shown.text.isNotEmpty(),
                    onClick = { field.clearText() },
                )
            }
        } else {
            null
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
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
private fun TextFieldValue.withText(text: String): TextFieldValue {
    if (this.text == text) return this
    val wasCursorAtEnd = (this.selection.collapsed && this.selection.end == this.text.length)
    val selection = when {
        wasCursorAtEnd -> TextRange(text.length)
        else -> this.selection // the constructor fits it into the new text
    }
    return TextFieldValue(text = text, selection = selection)
}
