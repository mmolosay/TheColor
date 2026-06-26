package io.github.mmolosay.thecolor.presentation.input.rgb

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.common.compose.thenIf
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.UiComponents.ProcessColorSubmissionResultAsSideEffect
import io.github.mmolosay.thecolor.presentation.input.UiComponents.onBackspace
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextField
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldAction
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings

@Composable
fun ColorInputRgb(
    viewModel: ColorInputRgbViewModel,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputRgbUiStrings(context) }
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value

    ColorInputRgb(
        data = data,
        strings = strings,
        execute = viewModel::execute,
    )
}

@Composable
fun ColorInputRgb(
    data: ColorInputRgbData,
    strings: ColorInputRgbUiStrings,
    execute: (ColorInputRgbAction) -> Unit,
) {
    Row {
        @Composable
        fun SpacerInBetween() = Spacer(modifier = Modifier.width(16.dp))

        val modifier = Modifier.weight(1f)
        val isSmartBackspaceEnabled = data.isSmartBackspaceEnabled
        fun execute(
            textFieldAction: TextFieldAction,
            component: RgbComponent,
        ) {
            val action = ColorInputRgbAction.TextField(
                wrapped = textFieldAction,
                component = component,
            )
            execute(action)
        }

        // R
        ComponentAdvancedTextField(
            modifier = modifier,
            data = data.rTextField,
            strings = strings.rTextField,
            execute = { textFieldAction ->
                execute(textFieldAction, RgbComponent.R)
            },
            imeAction = ImeAction.Next,
            hasPreviousComponent = false, // for R there's no previous
            enableSmartBackspace = isSmartBackspaceEnabled,
        )

        // G
        SpacerInBetween()
        ComponentAdvancedTextField(
            modifier = modifier,
            data = data.gTextField,
            strings = strings.gTextField,
            execute = { textFieldAction ->
                execute(textFieldAction, RgbComponent.G)
            },
            imeAction = ImeAction.Next,
            hasPreviousComponent = true, // for G previous is R
            enableSmartBackspace = isSmartBackspaceEnabled,
        )


        // B
        SpacerInBetween()
        ComponentAdvancedTextField(
            modifier = modifier,
            data = data.bTextField,
            strings = strings.bTextField,
            execute = { textFieldAction ->
                execute(textFieldAction, RgbComponent.B)
            },
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { execute(ColorInputRgbAction.SubmitInput) },
            ),
            hasPreviousComponent = true, // for B previous is G
            enableSmartBackspace = isSmartBackspaceEnabled,
        )
    }

    ProcessColorSubmissionResultAsSideEffect(
        ackResult = data.inputSubmissionResult,
    )
}

/**
 * A wrapper for [ComponentBasicTextField] with additional features, such as "smart backspace".
 */
@Composable
private fun ComponentAdvancedTextField(
    data: TextFieldData,
    strings: TextFieldUiStrings,
    execute: (TextFieldAction) -> Unit,
    imeAction: ImeAction,
    hasPreviousComponent: Boolean,
    enableSmartBackspace: Boolean,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    val focusManager = LocalFocusManager.current
    ComponentBasicTextField(
        modifier = modifier
            .thenIf(enableSmartBackspace) {
                onBackspace {
                    val text = data.text.data.string
                    if (text.isEmpty() && hasPreviousComponent) {
                        focusManager.moveFocus(FocusDirection.Previous)
                    }
            }
        },
        data = data,
        strings = strings,
        execute = execute,
        imeAction = imeAction,
        keyboardActions = keyboardActions,
    )
}

/**
 * A wrapper for a [TextField] that just manages [TextFieldValue].
 */
@Composable
private fun ComponentBasicTextField(
    modifier: Modifier = Modifier,
    data: TextFieldData,
    strings: TextFieldUiStrings,
    execute: (TextFieldAction) -> Unit,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    var value by remember {
        val text = data.text.data.string
        val value = TextFieldValue(
            text = text,
            selection = TextRange(index = text.length), // cursor at the end of the text
        )
        mutableStateOf(value)
    }
    TextField(
        modifier = modifier,
        data = data,
        strings = strings,
        execute = execute,
        value = value,
        onValueChange = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = keyboardActions,
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputRgb(
            data = previewData(),
            strings = previewUiStrings(),
            execute = {},
        )
    }
}

private fun previewData() =
    ColorInputRgbData(
        rTextField = TextFieldData(
            text = Text("12") causedByUser false,
            inputProcessor = { Text(it) },
            shouldSelectAllTextOnFocus = false,
            isClearTextFeatureEnabled = false,
        ),
        gTextField = TextFieldData(
            text = Text("") causedByUser false,
            inputProcessor = { Text(it) },
            shouldSelectAllTextOnFocus = false,
            isClearTextFeatureEnabled = false,
        ),
        bTextField = TextFieldData(
            text = Text("255") causedByUser false,
            inputProcessor = { Text(it) },
            shouldSelectAllTextOnFocus = false,
            isClearTextFeatureEnabled = false,
        ),
        inputSubmissionResult = null,
        isSmartBackspaceEnabled = true,
    )

private fun previewUiStrings() =
    ColorInputRgbUiStrings(
        rTextField = TextFieldUiStrings(
            label = "R",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
        gTextField = TextFieldUiStrings(
            label = "G",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
        bTextField = TextFieldUiStrings(
            label = "B",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
    )