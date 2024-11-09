package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.thenIf
import io.github.mmolosay.thecolor.presentation.input.impl.UiComponents
import io.github.mmolosay.thecolor.presentation.input.impl.UiComponents.Loading
import io.github.mmolosay.thecolor.presentation.input.impl.UiComponents.onBackspace
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.hideSoftwareKeyboardCommandOrNull

@Composable
fun ColorInputRgb(
    viewModel: ColorInputRgbViewModel,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputRgbUiStrings(context) }
    val state = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    val colorSubmissionResult =
        viewModel.colorSubmissionResultFlow.collectAsStateWithLifecycle().value
    val keyboardController = LocalSoftwareKeyboardController.current

    when (state) {
        is DataState.BeingInitialized ->
            Loading()
        is DataState.Ready -> {
            val uiData = ColorInputRgbUiData(state.data, strings)
            ColorInputRgb(uiData)
        }
    }

    val hideSoftwareKeyboardCommand = hideSoftwareKeyboardCommandOrNull(colorSubmissionResult)
    LaunchedEffect(hideSoftwareKeyboardCommand) {
        val command = hideSoftwareKeyboardCommand ?: return@LaunchedEffect
        keyboardController?.hide()
        command.onExecuted()
    }
}

@Composable
fun ColorInputRgb(
    uiData: ColorInputRgbUiData,
) {
    Row {
        @Composable
        fun SpacerInBetween() = Spacer(modifier = Modifier.width(16.dp))

        val modifier = Modifier.weight(1f)

        // R
        ComponentAdvancedTextField(
            modifier = modifier,
            uiData = uiData.rTextField,
            imeAction = ImeAction.Next,
            hasPreviousComponent = false, // for R there's no previous
            addSmartBackspaceModifier = uiData.addSmartBackspaceModifier,
        )

        // G
        SpacerInBetween()
        ComponentAdvancedTextField(
            modifier = modifier,
            uiData = uiData.gTextField,
            imeAction = ImeAction.Next,
            hasPreviousComponent = true, // for G previous is R
            addSmartBackspaceModifier = uiData.addSmartBackspaceModifier,
        )


        // B
        SpacerInBetween()
        ComponentAdvancedTextField(
            modifier = modifier,
            uiData = uiData.bTextField,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { uiData.onImeActionDone() },
            ),
            hasPreviousComponent = true, // for B previous is G
            addSmartBackspaceModifier = uiData.addSmartBackspaceModifier,
        )
    }
}

/**
 * A wrapper for [ComponentBasicTextField] with additional features.
 */
@Composable
private fun ComponentAdvancedTextField(
    modifier: Modifier = Modifier,
    uiData: TextFieldUiData,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions = KeyboardActions(),
    hasPreviousComponent: Boolean,
    addSmartBackspaceModifier: Boolean,
) {
    val focusManager = LocalFocusManager.current
    ComponentBasicTextField(
        modifier = modifier
            .thenIf(addSmartBackspaceModifier) {
                onBackspace {
                    val text = uiData.text.string
                    if (text.isEmpty() && hasPreviousComponent) {
                        focusManager.moveFocus(FocusDirection.Previous)
                    }
            }
        },
        uiData = uiData,
        imeAction = imeAction,
        keyboardActions = keyboardActions,
    )
}

/**
 * A wrapper for [UiComponents.TextField] that just manages [TextFieldValue].
 */
@Composable
private fun ComponentBasicTextField(
    modifier: Modifier = Modifier,
    uiData: TextFieldUiData,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    var value by remember {
        val text = uiData.text.string
        val value = TextFieldValue(
            text = text,
            selection = TextRange(index = text.length), // cursor at the end of the text
        )
        mutableStateOf(value)
    }
    UiComponents.TextField(
        modifier = modifier,
        uiData = uiData,
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
            uiData = previewUiData(),
        )
    }
}

private fun previewUiData() =
    ColorInputRgbUiData(
        rTextField = TextFieldUiData(
            text = Text("12"),
            onTextChange = {},
            filterUserInput = { Text(it) },
            label = "R",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        gTextField = TextFieldUiData(
            text = Text(""),
            onTextChange = {},
            filterUserInput = { Text(it) },
            label = "G",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        bTextField = TextFieldUiData(
            text = Text("255"),
            onTextChange = {},
            filterUserInput = { Text(it) },
            label = "B",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        onImeActionDone = {},
        addSmartBackspaceModifier = true,
    )