package io.github.mmolosay.thecolor.presentation.home.input.rgb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputComponents.TextField
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData

@Composable
fun ColorInputRgb(
    uiData: ColorInputRgbUiData,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        @Composable
        fun SpacerInBetween() = Spacer(modifier = Modifier.width(16.dp))

        TextField(
            modifier = Modifier.weight(1f),
            uiData = uiData.rInputField,
            imeAction = ImeAction.Next,
        )

        SpacerInBetween()
        TextField(
            modifier = Modifier.weight(1f),
            uiData = uiData.gInputField,
            imeAction = ImeAction.Next,
        )

        SpacerInBetween()
        TextField(
            modifier = Modifier.weight(1f),
            uiData = uiData.bInputField,
            imeAction = ImeAction.Done,
        )
    }
}

@Composable
private fun TextField(
    modifier: Modifier = Modifier,
    uiData: ColorInputFieldUiData,
    imeAction: ImeAction,
) {
    var value by remember { mutableStateOf(TextFieldValue(text = uiData.text)) }
    TextField(
        modifier = modifier,
        uiData = uiData,
        value = value,
        updateValue = { new -> value = new },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = KeyboardType.Number,
        )
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
        rInputField = ColorInputFieldUiData(
            text = "12",
            onTextChange = {},
            processText = { it },
            label = "R",
            placeholder = "0",
            prefix = "",
            trailingButton = ColorInputFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        gInputField = ColorInputFieldUiData(
            text = "",
            onTextChange = {},
            processText = { it },
            label = "G",
            placeholder = "0",
            prefix = "",
            trailingButton = ColorInputFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        bInputField = ColorInputFieldUiData(
            text = "255",
            onTextChange = {},
            processText = { it },
            label = "B",
            placeholder = "0",
            prefix = "",
            trailingButton = ColorInputFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
    )