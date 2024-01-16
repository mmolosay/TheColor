package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.presentation.home.input.plus
import io.github.mmolosay.thecolor.presentation.R as CommonR

@Composable
internal fun ColorInputHex(
    vm: ColorInputHexViewModel = hiltViewModel(),
) {
    val viewModelData = vm.uiDataFlow.collectAsStateWithLifecycle().value
    val textFieldUiData = viewModelData + rememberViewData()
    val uiData = ColorInputHexUiData(textFieldUiData)
    ColorInputHex(
        uiData = uiData,
    )
}

@Composable
internal fun ColorInputHex(
    modifier: Modifier = Modifier,
    uiData: ColorInputHexUiData,
) {
    var value by remember { mutableStateOf(TextFieldValue(text = uiData.inputField.text)) }
    OutlinedTextField(
        modifier = modifier
            .selectAllTextOnFocus(
                value = value,
                onValueChange = { new -> value = new },
            ),
        value = value,
        onValueChange = { new ->
            value = new
            uiData.inputField.onTextChange(new.text)
        },
        label = { Label(uiData.inputField.label) },
        placeholder = { Placeholder(uiData.inputField.placeholder) },
        trailingIcon = { TrailingButton(uiData.inputField.trailingButton) },
        prefix = { Prefix(uiData.inputField.prefix) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(),
        singleLine = true,
    )
    LaunchedEffect(uiData.inputField.text) {
        value = value.copy(text = uiData.inputField.text)
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
    )

@Composable
private fun TrailingButton(uiData: TrailingButton) =
    when (uiData) {
        is TrailingButton.Visible -> ClearIconButton(uiData)
        is TrailingButton.Hidden -> Unit // don't show anything
    }

@Composable
private fun ClearIconButton(uiData: TrailingButton.Visible) {
    IconButton(
        onClick = uiData.onClick,
    ) {
        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = uiData.iconContentDesc,
        )
    }
}

@Composable
private fun Prefix(text: String) =
    Text(
        text = text,
    )

private fun Modifier.selectAllTextOnFocus(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
) =
    onFocusChanged a@{
        if (!it.isFocused) return@a
        val text = value.text
        val newValue = value.copy(
            selection = TextRange(start = 0, end = text.length)
        )
        onValueChange(newValue)
    }

@Composable
private fun rememberViewData() =
    ColorInputFieldUiData.ViewData(
        label = stringResource(R.string.color_input_hex_label),
        placeholder = stringResource(R.string.color_input_hex_placeholder),
        prefix = stringResource(CommonR.string.color_hex_numbersign),
        trailingIconContentDesc = stringResource(R.string.color_input_hex_trailing_icon_desc),
    ).let {
        remember { it }
    }

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
        inputField = ColorInputFieldUiData(
            text = "",
            onTextChange = {},
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingButton = TrailingButton.Visible(onClick = {}, iconContentDesc = ""),
        )
    )