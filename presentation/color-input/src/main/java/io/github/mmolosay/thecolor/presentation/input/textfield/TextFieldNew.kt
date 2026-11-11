package io.github.mmolosay.thecolor.presentation.input.textfield

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.foundation.text.input.TextFieldState as ComposeTextFieldState
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

// TODO: remove "New" suffix from file name when the old file is gone

@Composable
internal fun TextField(
    feature: TextFieldFeature,
    strings: TextFieldUiStrings,
    hasClearTextButton: Boolean,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler?,
    modifier: Modifier = Modifier,
) =
    TextField(
        modifier = modifier,
        viewData = feature.viewData,
        onEnterText = feature::enterText,
        onClearText = if (hasClearTextButton) feature::clearText else null,
        strings = strings,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
    )

@Composable
internal fun TextField(
    viewData: StateFlow<TextFieldViewData>,
    onEnterText: (text: String, expected: String) -> Unit,
    onClearText: (() -> Unit)?,
    strings: TextFieldUiStrings,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler?,
    modifier: Modifier = Modifier,
) {
    val data = viewData.collectAsStateWithLifecycle().value
    val state: ComposeTextFieldState = remember(viewData) {
        ComposeTextFieldState(initialText = data.text)
    }

    // A text can be set from elsewhere other than user input via UI, e.g. by the parent feature.
    // This collector writes such text into the buffer, in the same frame as other Views update.
    // The ViewData's 'version' prevents the ABA problem.
    LaunchedEffect(viewData, state) {
        viewData.collect {
            state.edit {
                val newText = viewData.value.text
                setWithCursor(newText)
            }
        }
    }

    // intended name shadowing to enforce usage of latest lambda
    val onEnterText by rememberUpdatedState(onEnterText)
    val inputTransformation = remember(viewData) {
        InputTransformation {
            val before = this.originalText.toString()
            val after = this.asCharSequence().toString()
            if (after != before) {
                onEnterText(after, before)
            }
            val newText = viewData.value.text
            setWithCursor(newText)
        }
    }

    TextField(
        modifier = modifier,
        state = state,
        strings = strings,
        selectAllTextOnFocus = data.selectAllTextOnFocus,
        onClearTextClick = run {
            // 'onClearText' is captured anew on every composition, doesn't need 'rememberUpdatedState()'
            if (onClearText == null) return@run null
            {
                onClearText()
                // The 'viewData' collector above writes the cleared text only after this click is
                // handled, so a key typed meanwhile would be made on the old text and dropped by
                // the field. Writing it into the buffer here makes the next key see the clear (read-your-writes).
                state.edit {
                    val newText = viewData.value.text
                    setWithCursor(newText)
                }
            }
        },
        inputTransformation = inputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
    )
}

@Composable
private fun TextField(
    state: ComposeTextFieldState,
    strings: TextFieldUiStrings,
    selectAllTextOnFocus: Boolean,
    onClearTextClick: (() -> Unit)?, // no clear button when 'null'
    inputTransformation: InputTransformation?,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler?,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    if (selectAllTextOnFocus) {
        SelectAllTextOnFocusAsSideEffect(
            interactionSource = interactionSource,
            state = state,
        )
    }

    OutlinedTextField(
        modifier = modifier,
        state = state,
        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif),
        label = { Label(text = strings.label) },
        placeholder = { Placeholder(text = strings.placeholder) },
        trailingIcon = run {
            if (onClearTextClick == null) return@run null
            icon@{
                ClearTextTrailingButton(
                    visible = state.text.isNotEmpty(),
                    onClick = onClearTextClick,
                    iconContentDesc = strings.trailingIconContentDesc ?: return@icon,
                )
            }
        },
        prefix = run {
            if (strings.prefix == null) return@run null
            {
                Prefix(text = strings.prefix)
            }
        },
        inputTransformation = inputTransformation,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = TextFieldLineLimits.SingleLine,
        interactionSource = interactionSource,
    )
}

@Composable
private fun SelectAllTextOnFocusAsSideEffect(
    interactionSource: InteractionSource,
    state: ComposeTextFieldState,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(isFocused) {
        if (!isFocused) return@LaunchedEffect
        state.edit { selectAll() }
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
private fun ClearTextTrailingButton(
    visible: Boolean,
    onClick: () -> Unit,
    iconContentDesc: String,
) {
    val resizingAlignment = Alignment.Center
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandIn(expandFrom = resizingAlignment),
        exit = fadeOut() + shrinkOut(shrinkTowards = resizingAlignment),
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.testTag(TextFieldTestTags.ClearTextButton),
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

private fun TextFieldBuffer.setWithCursor(text: String) {
    if (this.asCharSequence().contentEquals(text)) return
    val wasCursorAtTheEnd = (this.selection.collapsed && this.selection.end == this.length)
    this.replace(0, this.length, text)
    if (wasCursorAtTheEnd) this.placeCursorAtEnd()
}

/*
 * At the moment, focus may work weirdly in the "Interactive mode".
 * Always test Compose changes in the app.
 */
@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    val viewData = remember {
        val value = TextFieldViewData(text = "1801FF", version = 0, selectAllTextOnFocus = true)
        MutableStateFlow(value)
    }
    TheColorTheme {
        Surface {
            TextField(
                viewData = viewData,
                onEnterText = { text, _ ->
                    viewData.update {
                        it.copy(text = text, version = it.version + 1)
                    }
                },
                onClearText = {
                    viewData.update {
                        it.copy(text = "", version = it.version + 1)
                    }
                },
                strings = TextFieldUiStrings(
                    label = "HEX",
                    placeholder = "000000",
                    prefix = "#",
                    trailingIconContentDesc = "Clear text field",
                ),
                keyboardOptions = KeyboardOptions.Default,
                onKeyboardAction = {},
            )
        }
    }
}

internal object TextFieldTestTags {
    const val ClearTextButton = "TextFieldTestTags.ClearTextButton"
}