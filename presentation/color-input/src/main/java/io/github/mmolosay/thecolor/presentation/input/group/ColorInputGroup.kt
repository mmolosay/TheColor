package io.github.mmolosay.thecolor.presentation.input.group

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupViewModel.DataState
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHex
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexData
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexUiStrings
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsv
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvData
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgb
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbData
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbUiStrings
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.domain.color.Color as DomainColor
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@Composable
fun ColorInputGroup(
    viewModel: ColorInputGroupViewModel,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputGroupUiStrings(context) }
    val dataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    when (dataState) {
        is DataState.Loading -> {
            // should promptly change to 'Ready', don't show loading indicator to avoid flashing
            doNothing()
        }
        is DataState.Ready -> {
            ColorInputGroup(
                data = dataState.data,
                strings = strings,
                hexInput = {
                    ColorInputHex(viewModel = viewModel.hexViewModel)
                },
                rgbInput = {
                    ColorInputRgb(viewModel = viewModel.rgbViewModel)
                },
                hsvInput = {
                    ColorInputHsv(viewModel = viewModel.hsvViewModel)
                },
            )
        }
    }
}

@Composable
fun ColorInputGroup(
    data: ColorInputGroupData,
    strings: ColorInputGroupUiStrings,
    hexInput: @Composable () -> Unit,
    rgbInput: @Composable () -> Unit,
    hsvInput: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = data.selectedInputType,
            transitionSpec = {
                fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
            },
            label = "Input type animated content",
        ) { type ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            ) {
                when (type) {
                    DomainColorInputType.Hex -> hexInput()
                    DomainColorInputType.Rgb -> rgbInput()
                    DomainColorInputType.Hsv -> hsvInput()
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        InputSelector(
            data = data,
            strings = strings,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputSelector(
    data: ColorInputGroupData,
    strings: ColorInputGroupUiStrings,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        data.orderedInputTypes.forEach { type ->
            val isSelected = (type == data.selectedInputType)
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
                onClick = { data.onInputTypeChange(type) },
                label = {
                    val labelText = type.label(strings)
                    ChipLabel(text = labelText)
                },
                colors = colors,
                border = border,
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

private fun DomainColorInputType.label(strings: ColorInputGroupUiStrings): String =
    when (this) {
        DomainColorInputType.Hex -> strings.hexLabel
        DomainColorInputType.Rgb -> strings.rgbLabel
        DomainColorInputType.Hsv -> strings.hsvLabel
    }

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface {
            ColorInputGroup(
                data = previewData(),
                strings = previewUiStrings(),
                hexInput = {
                    ColorInputHex(
                        data = previewHexData(),
                        strings = previewHexUiStrings(),
                    )
                },
                rgbInput = {
                    ColorInputRgb(
                        data = previewRgbData(),
                        strings = previewRgbUiStrings(),
                    )
                },
                hsvInput = {
                    ColorInputHsv(
                        data = previewHsvData(),
                    )
                },
            )
        }
    }
}

private fun previewData() =
    ColorInputGroupData(
        selectedInputType = DomainColorInputType.Hex,
        orderedInputTypes = listOf(
            DomainColorInputType.Hex,
            DomainColorInputType.Rgb,
        ),
        onInputTypeChange = {},
    )

private fun previewUiStrings() =
    ColorInputGroupUiStrings(
        hexLabel = "HEX",
        rgbLabel = "RGB",
        hsvLabel = "HSV",
    )

private fun previewHexData() =
    ColorInputHexData(
        textField = TextFieldData(
            text = TextFieldData.Text("") causedByUser false,
            onTextChange = {},
            filterUserInput = { TextFieldData.Text(it) },
            clearText = TextFieldData.NoOpClearTextFeature,
            shouldSelectAllTextOnFocus = false,
        ),
        submitInput = {},
    )

private fun previewHexUiStrings() =
    ColorInputHexUiStrings(
        textField = TextFieldUiStrings(
            label = "HEX",
            placeholder = "000000",
            prefix = "#",
            trailingIconContentDesc = "Clear text",
        ),
    )

private fun previewRgbData() =
    ColorInputRgbData(
        rTextField = TextFieldData(
            text = TextFieldData.Text("") causedByUser false,
            onTextChange = {},
            filterUserInput = { TextFieldData.Text(it) },
            clearText = null,
            shouldSelectAllTextOnFocus = false,
        ),
        gTextField = TextFieldData(
            text = TextFieldData.Text("") causedByUser false,
            onTextChange = {},
            filterUserInput = { TextFieldData.Text(it) },
            clearText = null,
            shouldSelectAllTextOnFocus = false,
        ),
        bTextField = TextFieldData(
            text = TextFieldData.Text("") causedByUser false,
            onTextChange = {},
            filterUserInput = { TextFieldData.Text(it) },
            clearText = null,
            shouldSelectAllTextOnFocus = false,
        ),
        submitInput = {},
        isSmartBackspaceEnabled = true,
    )

private fun previewRgbUiStrings() =
    ColorInputRgbUiStrings(
        rTextField = TextFieldUiStrings(
            label = "R",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
        gTextField = TextFieldUiStrings(
            label = "G",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
        bTextField = TextFieldUiStrings(
            label = "B",
            placeholder = "0",
            prefix = null,
            trailingIconContentDesc = null,
        ),
    )

private fun previewHsvData() =
    ColorInputHsvData(
        color = DomainColor.Hsv(hue = 259f, saturation = 0.65f, value = 0.82f),
        onColorChanged = {},
    )