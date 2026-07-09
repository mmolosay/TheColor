package io.github.mmolosay.thecolor.presentation.input.rgb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldInputProcessor
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings

@Suppress("unused") // example of how Facade is obtained from ViewModel
@Composable
fun ColorInputRgb(
    viewModel: ColorInputRgbViewModel,
) {
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value
    val facade = remember(data, viewModel) { viewModel.facadeFactory.create(data) }

    ColorInputRgb(
        facade = facade,
    )
}

@Composable
fun ColorInputRgb(
    facade: ColorInputRgbFacade,
    strings: ColorInputRgbUiStrings = rememberColorInputRgbUiStrings(),
) {
    val execute by rememberUpdatedState(facade.execute) // reference 'execute' directly to enable lambda memoization
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val modifier = Modifier.weight(1f)
        val isSmartBackspaceEnabled = facade.isSmartBackspaceEnabled

        // R
        ComponentAdvancedTextField(
            modifier = modifier,
            facade = facade.rTextField,
            strings = strings.rTextField,
            imeAction = ImeAction.Next,
            hasPreviousComponent = false, // for R there's no previous
            enableSmartBackspace = isSmartBackspaceEnabled,
        )

        // G
        ComponentAdvancedTextField(
            modifier = modifier,
            facade = facade.gTextField,
            strings = strings.gTextField,
            imeAction = ImeAction.Next,
            hasPreviousComponent = true, // for G previous is R
            enableSmartBackspace = isSmartBackspaceEnabled,
        )


        // B
        ComponentAdvancedTextField(
            modifier = modifier,
            facade = facade.bTextField,
            strings = strings.bTextField,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    val action = ColorInputRgbAction.SubmitInput
                    execute(action)
                },
            ),
            hasPreviousComponent = true, // for B previous is G
            enableSmartBackspace = isSmartBackspaceEnabled,
        )
    }

    ProcessColorSubmissionResultAsSideEffect(
        result = facade.inputSubmissionResult,
        ack = { execute(ColorInputRgbAction.AckInputSubmissionResult) },
    )
}

/**
 * A wrapper for [ComponentBasicTextField] with additional features, such as "smart backspace".
 */
@Composable
private fun ComponentAdvancedTextField(
    facade: TextFieldFacade,
    strings: TextFieldUiStrings,
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
                    val text = facade.text.data.string
                    if (text.isEmpty() && hasPreviousComponent) {
                        focusManager.moveFocus(FocusDirection.Previous)
                    }
            }
        },
        facade = facade,
        strings = strings,
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
    facade: TextFieldFacade,
    strings: TextFieldUiStrings,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    var value by remember {
        val text = facade.text.data.string
        val value = TextFieldValue(
            text = text,
            selection = TextRange(index = text.length), // cursor at the end of the text
        )
        mutableStateOf(value)
    }
    TextField(
        modifier = modifier,
        facade = facade,
        strings = strings,
        value = value,
        onValueChange = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = keyboardActions,
    )
}

@Composable
private fun rememberColorInputRgbUiStrings(): ColorInputRgbUiStrings {
    val context = LocalContext.current
    return remember(context) { ColorInputRgbUiStrings(context) }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputRgb(
            facade = previewFacade(),
            strings = previewUiStrings(),
        )
    }
}

private fun previewFacade() =
    ColorInputRgbFacade(
        rTextField = TextFieldFacade(
            execute = {},
            text = Text("12") causedByUser true,
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = false,
            inputProcessor = TextFieldInputProcessor { Text(it) },
        ),
        gTextField = TextFieldFacade(
            execute = {},
            text = Text("") causedByUser true,
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = false,
            inputProcessor = TextFieldInputProcessor { Text(it) },
        ),
        bTextField = TextFieldFacade(
            execute = {},
            text = Text("255") causedByUser true,
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = false,
            inputProcessor = TextFieldInputProcessor { Text(it) },
        ),
        execute = {},
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