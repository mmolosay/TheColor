package io.github.mmolosay.thecolor.presentation.input.hex

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.UiComponents.ProcessSubmissionAnswerAsSideEffect
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.textfield.TextField
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Composable
fun ColorInputHex(
    feature: ColorInputHexFeature,
    strings: ColorInputHexUiStrings = rememberColorInputHexUiStrings(),
) =
    ColorInputHex(
        viewData = feature.viewData,
        onSubmit = feature::submit,
        onAckAnswer = feature::ackAnswer,
        textFieldViewData = feature.textField.viewData,
        onTextFieldEnterText = feature.textField::enterText,
        onTextFieldClearText = feature.textField::clearText,
        strings = strings,
    )

@Composable
fun ColorInputHex(
    viewData: StateFlow<ColorInputHexViewData>,
    onSubmit: () -> Unit,
    onAckAnswer: (ColorInputSubmission.Answered) -> Unit,
    textFieldViewData: StateFlow<TextFieldViewData>,
    onTextFieldEnterText: (text: String, expected: String) -> Unit,
    onTextFieldClearText: () -> Unit,
    strings: ColorInputHexUiStrings,
) {
    val data = viewData.collectAsStateWithLifecycle().value

    TextField(
        modifier = Modifier
            .defaultMinSize(minWidth = 180.dp)
            .fillMaxWidth(0.5f),
        viewData = textFieldViewData,
        onEnterText = onTextFieldEnterText,
        onClearText = onTextFieldClearText,
        strings = strings.textField,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Characters,
        ),
        onKeyboardAction = { onSubmit() },
    )

    ProcessSubmissionAnswerAsSideEffect(
        submission = data.answeredSubmission,
        rejectionMessage = strings.invalidSubmittedColorMessage,
        onProcessed = onAckAnswer,
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
    val viewData = remember {
        val value = ColorInputHexViewData(answeredSubmission = null)
        MutableStateFlow(value)
    }
    val textFieldViewData = remember {
        val value = TextFieldViewData(text = "1A803F", version = 0, selectAllTextOnFocus = true)
        MutableStateFlow(value)
    }
    TheColorTheme {
        ColorInputHex(
            viewData = viewData,
            onSubmit = {},
            onAckAnswer = {},
            textFieldViewData = textFieldViewData,
            onTextFieldEnterText = { text, _ ->
                textFieldViewData.update {
                    it.copy(text = text, version = it.version + 1)
                }
            },
            onTextFieldClearText = {
                textFieldViewData.update {
                    it.copy(text = "", version = it.version + 1)
                }
            },
            strings = previewUiStrings(),
        )
    }
}

private fun previewUiStrings() =
    ColorInputHexUiStrings(
        textField = TextFieldUiStrings(
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingIconContentDesc = "Clear text",
        ),
        invalidSubmittedColorMessage = "Please enter a valid color",
    )