package io.github.mmolosay.thecolor.presentation.input.hex

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.UiComponents.DataStateCrossfade
import io.github.mmolosay.thecolor.presentation.input.UiComponents.ProcessColorSubmissionResultAsSideEffect
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextField
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings
import io.github.mmolosay.thecolor.utils.NoOpActionWithResult
import io.github.mmolosay.thecolor.utils.invoke

@Composable
fun ColorInputHex(
    viewModel: ColorInputHexViewModel,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputHexUiStrings(context) }
    val dataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value

    DataStateCrossfade(
        actualDataState = dataState,
    ) { state ->
        when (state) {
            is DataState.BeingInitialized ->
                ColorInputHexLoading()
            is DataState.Ready -> {
                ColorInputHex(
                    data = state.data,
                    strings = strings,
                )
            }
        }
    }
}

@Composable
fun ColorInputHex(
    data: ColorInputHexData,
    strings: ColorInputHexUiStrings,
) {
    var value by remember {
        val text = data.textField.text.data.string
        val value = TextFieldValue(
            text = text,
            selection = TextRange(index = text.length), // cursor at the end of the text
        )
        mutableStateOf(value)
    }

    TextField(
        modifier = Modifier
            .defaultMinSize(minWidth = 180.dp)
            .fillMaxWidth(0.5f),
        data = data.textField,
        strings = strings.textField,
        value = value,
        onValueChange = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Characters,
        ),
        keyboardActions = KeyboardActions(
            onDone = { data.submitInput() },
        ),
    )

    ProcessColorSubmissionResultAsSideEffect(
        ackResult = data.submitInput.result,
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputHex(
            data = previewData(),
            strings = previewUiStrings(),
        )
    }
}

private fun previewData() =
    ColorInputHexData(
        textField = TextFieldData(
            text = Text("") causedByUser false,
            onTextChange = {},
            filterUserInput = { Text(it) },
            clearText = TextFieldData.NoOpClearTextFeature,
            shouldSelectAllTextOnFocus = false,
        ),
        submitInput = NoOpActionWithResult(),
    )

private fun previewUiStrings() =
    ColorInputHexUiStrings(
        textField = TextFieldUiStrings(
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingIconContentDesc = "Clear text",
        ),
    )