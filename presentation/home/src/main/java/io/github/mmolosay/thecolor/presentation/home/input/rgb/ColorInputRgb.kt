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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputComponents.TextField
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.field.ColorInputFieldUiData.Text

@Composable
fun ColorInputRgb(
    vm: ColorInputRgbViewModel,
) {
    val uiData = vm.uiDataFlow.collectAsStateWithLifecycle().value
    ColorInputRgb(
        uiData = uiData,
    )
}

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
    var value by remember { mutableStateOf(TextFieldValue(text = uiData.text.string)) }
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
            text = Text("12"),
            onTextChange = {},
            filterUserInput = { Text(it) },
            label = "R",
            placeholder = "0",
            prefix = "",
            trailingButton = ColorInputFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        gInputField = ColorInputFieldUiData(
            text = Text(""),
            onTextChange = {},
            filterUserInput = { Text(it) },
            label = "G",
            placeholder = "0",
            prefix = "",
            trailingButton = ColorInputFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        bInputField = ColorInputFieldUiData(
            text = Text("255"),
            onTextChange = {},
            filterUserInput = { Text(it) },
            label = "B",
            placeholder = "0",
            prefix = "",
            trailingButton = ColorInputFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
    )