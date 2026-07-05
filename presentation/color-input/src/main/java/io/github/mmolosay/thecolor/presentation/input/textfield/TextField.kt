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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import androidx.compose.ui.text.input.TextFieldValue as MaterialTextFieldValue
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

/**
 * A text field used by various 'Color Input' Views.
 * Presents [TextFieldData].
 */
@Composable
internal fun TextField(
    facade: TextFieldFacade,
    strings: TextFieldUiStrings,
    value: MaterialTextFieldValue,
    onValueChange: (MaterialTextFieldValue) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    if (facade.data.shouldSelectAllTextOnFocus) {
        SelectAllTextOnFocusAsSideEffect(
            interactionSource = interactionSource,
            value = value,
            onValueChange = onValueChange,
        )
    }
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { new ->
            val current = value
            if (current.text != new.text) {
                // can't just pass new.text to ViewModel for filtering: TextFieldValue.selection will be lost
                val newInput = new.text
                val newText = facade.inputProcessor(newInput)
                val newValue = new.copy(text = newText.string)
                onValueChange(newValue)
                run {
                    val action = TextFieldAction.SetText(newText)
                    facade.execute(action)
                }
            } else {
                onValueChange(new)
            }
        },
        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.SansSerif),
        label = { Label(text = strings.label) },
        placeholder = { Placeholder(text = strings.placeholder) },
        trailingIcon = icon@{
            if (facade.data.isClearTextFeatureEnabled.not()) return@icon
            ClearTextTrailingButton(
                visible = value.text.isNotEmpty(),
                onClick = run {
                    val execute = facade.execute // 'Facade' instance may change, but 'execute' won't
                    return@run {
                        val action = TextFieldAction.ClearTextFeature.Invoke
                        execute(action)
                    }
                },
                iconContentDesc = strings.trailingIconContentDesc ?: return@icon,
            )
        },
        prefix = if (strings.prefix != null)
            ({ Prefix(text = strings.prefix) })
        else null,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        interactionSource = interactionSource,
    )
    // for when text is changed programmatically
    LaunchedEffect(facade.data.text) {
        val oldValue = value
        val newText = facade.data.text.data.string
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

@Composable
private fun SelectAllTextOnFocusAsSideEffect(
    interactionSource: InteractionSource,
    value: MaterialTextFieldValue,
    onValueChange: (MaterialTextFieldValue) -> Unit,
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(isFocused) {
        if (!isFocused) return@LaunchedEffect
        val text = value.text
        val newValue = value.copy(selection = TextRange(start = 0, end = text.length))
        onValueChange(newValue)
    }
}

/*
 * At the moment, focus may work weirdly in the "Interactive mode".
 * Always test Compose changes in the app.
 */
@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface {
            val textState = remember { mutableStateOf(Text("1801FF")) }
            var value by remember {
                mutableStateOf(MaterialTextFieldValue(text = textState.value.string))
            }
            TextField(
                facade = TextFieldFacade(
                    data = TextFieldData(
                        text = textState.value causedByUser true,
                        shouldSelectAllTextOnFocus = true,
                        isClearTextFeatureEnabled = true,
                    ),
                    execute = {},
                    inputProcessor = TextFieldInputProcessor { Text(it) },
                ),
                strings = TextFieldUiStrings(
                    label = "HEX",
                    placeholder = "000000",
                    prefix = "#",
                    trailingIconContentDesc = "Clear text field",
                ),
                value = value,
                onValueChange = { newValue -> value = newValue },
                keyboardOptions = KeyboardOptions.Default,
                keyboardActions = KeyboardActions(),
            )
        }
    }
}