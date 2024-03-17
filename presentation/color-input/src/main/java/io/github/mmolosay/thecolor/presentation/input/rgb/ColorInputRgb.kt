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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.UiComponents.Loading
import io.github.mmolosay.thecolor.presentation.input.UiComponents.TextField
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbUiData.ViewData

@Composable
fun ColorInputRgb(
    vm: ColorInputRgbViewModel,
) {
    val viewData = rememberViewData()
    val state = vm.dataStateFlow.collectAsStateWithLifecycle().value
    when (state) {
        is DataState.BeingInitialized -> Loading()
        is DataState.Ready -> {
            val uiData = rememberUiData(data = state.data, viewData = viewData)
            ColorInputRgb(uiData)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorInputRgb(
    uiData: ColorInputRgbUiData,
) {
    Row {
        @Composable
        fun SpacerInBetween() = Spacer(modifier = Modifier.width(16.dp))

        val keyboardController = LocalSoftwareKeyboardController.current
        val modifier = Modifier.weight(1f)

        TextField(
            modifier = modifier,
            uiData = uiData.rTextField,
            imeAction = ImeAction.Next,
        )

        SpacerInBetween()
        TextField(
            modifier = modifier,
            uiData = uiData.gTextField,
            imeAction = ImeAction.Next,
        )

        SpacerInBetween()
        TextField(
            modifier = modifier,
            uiData = uiData.bTextField,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    uiData.onImeActionDone()
                },
            ),
        )
    }
}

@Composable
private fun TextField(
    modifier: Modifier = Modifier,
    uiData: TextFieldUiData,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions = KeyboardActions(),
) {
    var value by remember { mutableStateOf(TextFieldValue(text = uiData.text.string)) }
    TextField(
        modifier = modifier,
        uiData = uiData,
        value = value,
        updateValue = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = keyboardActions,
    )
}

@Composable
private fun rememberViewData(): ViewData {
    val context = LocalContext.current
    return remember { ColorInputRgbViewData(context) }
}

@Composable
private fun rememberUiData(
    data: ColorInputRgbData,
    viewData: ViewData,
): ColorInputRgbUiData =
    remember(data) { data + viewData }

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
    )