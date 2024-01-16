package io.github.mmolosay.thecolor.presentation.home.input.hex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputComponents.TextField
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.TrailingButton

@Composable
internal fun ColorInputHex(
    vm: ColorInputHexViewModel,
) {
    val uiData = vm.uiDataFlow.collectAsStateWithLifecycle().value
    ColorInputHex(
        uiData = uiData,
    )
}

@Composable
internal fun ColorInputHex(
    uiData: ColorInputHexUiData,
) {
    var value by remember { mutableStateOf(TextFieldValue(text = uiData.inputField.text)) }
    TextField(
        uiData = uiData.inputField,
        value = value,
        updateValue = { new -> value = new },
    )
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
            processText = { it },
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingButton = TrailingButton.Visible(onClick = {}, iconContentDesc = ""),
        )
    )