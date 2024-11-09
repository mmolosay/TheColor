package io.github.mmolosay.thecolor.presentation.input.impl

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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputViewModel.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHex
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexUiData
import io.github.mmolosay.thecolor.presentation.input.impl.rgb.ColorInputRgb
import io.github.mmolosay.thecolor.presentation.input.impl.rgb.ColorInputRgbUiData
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

@Composable
fun ColorInput(
    viewModel: ColorInputViewModel,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputUiStrings(context) }
    val dataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    when (dataState) {
        is DataState.Loading -> {
            // should promptly change to 'Ready', don't show loading indicator to avoid flashing
            doNothing()
        }
        is DataState.Ready -> {
            val uiData = ColorInputUiData(dataState.data, strings)
            ColorInput(
                uiData = uiData,
                hexInput = {
                    ColorInputHex(viewModel = viewModel.hexViewModel)
                },
                rgbInput = {
                    ColorInputRgb(viewModel = viewModel.rgbViewModel)
                },
            )
        }
    }
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
            targetState = uiData.selectedInputType,
            label = "Input type cross-fade",
        ) { type ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            ) {
                when (type) {
                    DomainColorInputType.Hex -> hexInput()
                    DomainColorInputType.Rgb -> rgbInput()
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
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
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentEnforcement provides false,
        ) {
            uiData.orderedInputTypes.forEach { type ->
                val isSelected = (type == uiData.selectedInputType)
                val contentColor = LocalContentColor.current
                val colors = FilterChipDefaults.filterChipColors(
                    labelColor = contentColor.copy(alpha = 0.60f),
                    // selectedLabelColor as default
                )
                val border = FilterChipDefaults.filterChipBorder(
                    enabled = true, // constant
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.60f),
                    // selectedBorderColor doesn't matter because it has 0 width
                )
                FilterChip(
                    selected = isSelected,
                    onClick = { uiData.onInputTypeChange(type) },
                    label = {
                        val labelText = type.label(uiData)
                        ChipLabel(text = labelText)
                    },
                    colors = colors,
                    border = border,
                )
            }
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

private fun DomainColorInputType.label(uiData: ColorInputUiData): String =
    when (this) {
        DomainColorInputType.Hex -> uiData.hexLabel
        DomainColorInputType.Rgb -> uiData.rgbLabel
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
        selectedInputType = DomainColorInputType.Hex,
        orderedInputTypes = DomainColorInputType.entries,
        onInputTypeChange = {},
        hexLabel = "HEX",
        rgbLabel = "RGB",
    )

private fun previewInputHexUiData() =
    ColorInputHexUiData(
        textField = TextFieldUiData(
            text = TextFieldData.Text(""),
            onTextChange = {},
            filterUserInput = { TextFieldData.Text(it) },
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        onImeActionDone = {},
    )

private fun previewInputRgbUiData() =
    ColorInputRgbUiData(
        rTextField = TextFieldUiData(
            text = TextFieldData.Text("12"),
            onTextChange = {},
            filterUserInput = { TextFieldData.Text(it) },
            label = "R",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        gTextField = TextFieldUiData(
            text = TextFieldData.Text(""),
            onTextChange = {},
            filterUserInput = { TextFieldData.Text(it) },
            label = "G",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        bTextField = TextFieldUiData(
            text = TextFieldData.Text("255"),
            onTextChange = {},
            filterUserInput = { TextFieldData.Text(it) },
            label = "B",
            placeholder = "0",
            prefix = "",
            trailingButton = TextFieldUiData.TrailingButton.Visible(
                onClick = {},
                iconContentDesc = "",
            ),
        ),
        onImeActionDone = {},
        addSmartBackspaceModifier = true,
    )