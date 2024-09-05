package io.github.mmolosay.thecolor.presentation.input.impl.hex

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.impl.UiComponents.Loading
import io.github.mmolosay.thecolor.presentation.input.impl.UiComponents.TextField
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexUiData.ViewData
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.hideSoftwareKeyboardCommandOrNull

@Composable
fun ColorInputHex(
    viewModel: ColorInputHexViewModel,
) {
    val viewData = rememberViewData()
    val state = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    val colorSubmissionResult = viewModel.colorSubmissionResultFlow.collectAsStateWithLifecycle().value
    val keyboardController = LocalSoftwareKeyboardController.current

    when (state) {
        is DataState.BeingInitialized ->
            Loading()
        is DataState.Ready -> {
            val uiData = rememberUiData(data = state.data, viewData = viewData)
            ColorInputHex(
                uiData = uiData,
            )
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
fun ColorInputHex(
    uiData: ColorInputHexUiData,
) {
    var value by remember { mutableStateOf(TextFieldValue(text = uiData.textField.text.string)) }

    TextField(
        modifier = Modifier.fillMaxWidth(0.5f),
        uiData = uiData.textField,
        value = value,
        updateValue = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Characters,
        ),
        keyboardActions = KeyboardActions(
            onDone = { uiData.onImeActionDone() },
        ),
    )
}

@Composable
private fun rememberViewData(): ViewData {
    val context = LocalContext.current
    return remember { ColorInputHexViewData(context) }
}

@Composable
private fun rememberUiData(
    data: ColorInputHexData,
    viewData: ViewData,
): ColorInputHexUiData =
    remember(data) { data + viewData } // TODO: do in background?

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputHex(
            uiData = previewUiData(),
        )
    }
}

private fun previewUiData() =
    ColorInputHexUiData(
        textField = TextFieldUiData(
            text = Text(""),
            onTextChange = {},
            filterUserInput = { Text(it) },
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingButton = TrailingButton.Visible(onClick = {}, iconContentDesc = ""),
        ),
        onImeActionDone = {},
    )