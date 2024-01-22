package io.github.mmolosay.thecolor.input

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.input.ColorInputUiData.ViewType
import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.hex.ColorInputHex
import io.github.mmolosay.thecolor.input.hex.ColorInputHexUiData
import io.github.mmolosay.thecolor.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgb
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbUiData
import io.github.mmolosay.thecolor.input.rgb.ColorInputRgbViewModel

@Composable
fun ColorInput(
    vm: ColorInputViewModel,
    hexViewModel: ColorInputHexViewModel,
    rgbViewModel: ColorInputRgbViewModel,
) {
    val uiData = vm.uiDataFlow.collectAsStateWithLifecycle().value
    ColorInput(
        uiData = uiData,
        hexInput = {
            ColorInputHex(vm = hexViewModel)
        },
        rgbInput = {
            ColorInputRgb(vm = rgbViewModel)
        },
    )
}

@Composable
fun ColorInput(
    uiData: ColorInputUiData,
    hexInput: @Composable () -> Unit,
    rgbInput: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Crossfade(
            targetState = uiData.viewType,
            label = "Input type cross-fade",
        ) { type ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            ) {
                when (type) {
                    ViewType.Hex -> hexInput()
                    ViewType.Rgb -> rgbInput()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        InputSelector(uiData)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputSelector(
    uiData: ColorInputUiData,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ViewType.entries.forEach { type ->
            val labelText = type.label(uiData)
            FilterChip(
                selected = (type == uiData.viewType),
                onClick = { uiData.onInputTypeChange(type) },
                label = { ChipLabel(labelText) }
            )
        }
    }
}

@Composable
private fun ChipLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelMedium,
    )
}

private fun ViewType.label(uiData: ColorInputUiData): String =
    when (this) {
        ViewType.Hex -> uiData.hexLabel
        ViewType.Rgb -> uiData.rgbLabel
    }

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInput(
            uiData = previewUiData(),
            hexInput = {
                ColorInputHex(uiData = previewInputHexUiData())
            },
            rgbInput = {
                ColorInputRgb(uiData = previewInputRgbUiData())
            },
        )
    }
}

private fun previewUiData() =
    ColorInputUiData(
        viewType = ViewType.Hex,
        onInputTypeChange = {},
        hexLabel = "HEX",
        rgbLabel = "RGB",
    )

private fun previewInputHexUiData() =
    ColorInputHexUiData(
        textField = TextFieldUiData(
            text = TextFieldUiData.Text(""),
            onTextChange = {},
            filterUserInput = { TextFieldUiData.Text(it) },
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        )
    )

private fun previewInputRgbUiData() =
    ColorInputRgbUiData(
        rTextField = TextFieldUiData(
            text = TextFieldUiData.Text("12"),
            onTextChange = {},
            filterUserInput = { TextFieldUiData.Text(it) },
            label = "R",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        gTextField = TextFieldUiData(
            text = TextFieldUiData.Text(""),
            onTextChange = {},
            filterUserInput = { TextFieldUiData.Text(it) },
            label = "G",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        bTextField = TextFieldUiData(
            text = TextFieldUiData.Text("255"),
            onTextChange = {},
            filterUserInput = { TextFieldUiData.Text(it) },
            label = "B",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
    )