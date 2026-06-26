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
import io.github.mmolosay.thecolor.presentation.input.UiComponents.ProcessColorSubmissionResultAsSideEffect
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextField
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings

@Composable
fun ColorInputHex(
    viewModel: ColorInputHexViewModel,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputHexUiStrings(context) }
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value

    ColorInputHex(
        data = data,
        strings = strings,
        execute = viewModel::execute,
    )
}

@Composable
fun ColorInputHex(
    data: ColorInputHexData,
    strings: ColorInputHexUiStrings,
    execute: (ColorInputHexAction) -> Unit,
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
        execute = { textFieldAction ->
            val action = ColorInputHexAction.TextField(textFieldAction)
            execute(action)
        },
        value = value,
        onValueChange = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Characters,
        ),
        keyboardActions = KeyboardActions(
            onDone = { execute(ColorInputHexAction.SubmitInput) },
        ),
    )

    ProcessColorSubmissionResultAsSideEffect(
        ackResult = data.inputSubmissionResult,
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputHex(
            data = previewData(),
            strings = previewUiStrings(),
            execute = {},
        )
    }
}

private fun previewData() =
    ColorInputHexData(
        textField = TextFieldData(
            text = Text("") causedByUser false,
            inputProcessor = { Text(it) },
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = true,
        ),
        inputSubmissionResult = null,
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