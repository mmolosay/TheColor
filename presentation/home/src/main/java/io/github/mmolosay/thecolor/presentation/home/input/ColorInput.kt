package io.github.mmolosay.thecolor.presentation.home.input

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
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputUiData.InputType
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHex
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexUiData
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexViewModel
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgb
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbUiData
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbViewModel

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
            targetState = uiData.inputType,
            label = "Input type cross-fade",
        ) { type ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            ) {
                when (type) {
                    InputType.Hex -> hexInput()
                    InputType.Rgb -> rgbInput()
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InputType.entries.forEach { type ->
            val labelText = type.label(uiData)
            FilterChip(
                selected = (type == uiData.inputType),
                onClick = { uiData.onInputTypeSelect(type) },
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

private fun InputType.label(uiData: ColorInputUiData): String =
    when (this) {
        InputType.Hex -> uiData.hexLabel
        InputType.Rgb -> uiData.rgbLabel
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
        inputType = InputType.Hex,
        onInputTypeSelect = {},
        hexLabel = "HEX",
        rgbLabel = "RGB",
    )

private fun previewInputHexUiData() =
    ColorInputHexUiData(
        inputField = ColorInputFieldUiData(
            text = "",
            onTextChange = {},
            processText = { it },
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingButton = ColorInputFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        )
    )

private fun previewInputRgbUiData() =
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