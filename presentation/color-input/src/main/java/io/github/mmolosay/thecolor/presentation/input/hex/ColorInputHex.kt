package io.github.mmolosay.thecolor.presentation.input.hex

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldInputProcessor
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings

@Suppress("unused") // example of how Facade is obtained from ViewModel
@Composable
fun ColorInputHex(
    viewModel: ColorInputHexViewModel,
) {
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value
    val facade = remember(data, viewModel) { viewModel.facadeFactory.create(data) }

    ColorInputHex(
        facade = facade,
    )
}

@Composable
fun ColorInputHex(
    facade: ColorInputHexFacade,
    strings: ColorInputHexUiStrings = rememberColorInputHexUiStrings(),
) {
    val execute by rememberUpdatedState(facade.execute) // reference 'execute' directly to enable lambda memoization
    var value by remember {
        val text = facade.textField.text.data.string
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
        facade = facade.textField,
        strings = strings.textField,
        value = value,
        onValueChange = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Characters,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                val action = ColorInputHexAction.SubmitInput
                execute(action)
            },
        ),
    )

    ProcessColorSubmissionResultAsSideEffect(
        result = facade.inputSubmissionResult,
        ack = { execute(ColorInputHexAction.AckInputSubmissionResult) },
    )
}

@Composable
private fun rememberColorInputHexUiStrings(): ColorInputHexUiStrings {
    val context = LocalContext.current
    return remember(context) { ColorInputHexUiStrings(context) }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputHex(
            facade = previewFacade(),
            strings = previewUiStrings(),
        )
    }
}

private fun previewFacade() =
    ColorInputHexFacade(
        textField = TextFieldFacade(
            execute = {},
            text = Text("1A803F") causedByUser true,
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = true,
            inputProcessor = TextFieldInputProcessor { Text(it) },
        ),
        execute = {},
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