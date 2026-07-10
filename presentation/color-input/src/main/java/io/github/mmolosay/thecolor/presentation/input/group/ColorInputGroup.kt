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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHex
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacade
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexUiStrings
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsv
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFacade
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgb
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacade
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbUiStrings
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldInputProcessor
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldUiStrings
import io.github.mmolosay.thecolor.domain.color.Color as DomainColor
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@Composable
fun ColorInputGroup(
    viewModel: ColorInputGroupViewModel,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputGroupUiStrings(context) }
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value
    val facade = remember(data, viewModel) { viewModel.facadeFactory.create(data) }

    ColorInputGroup(
        facade = facade,
        strings = strings,
        hexInput = {
            ColorInputHex(
                viewModel = viewModel.hexViewModel,
            )
        },
        rgbInput = {
            ColorInputRgb(
                viewModel = viewModel.rgbViewModel,
            )
        },
        hsvInput = {
            ColorInputHsv(
                viewModel = viewModel.hsvViewModel,
            )
        },
    )
}

@Composable
fun ColorInputGroup(
    facade: ColorInputGroupFacade,
    strings: ColorInputGroupUiStrings,
    hexInput: @Composable () -> Unit,
    rgbInput: @Composable () -> Unit,
    hsvInput: @Composable () -> Unit,
) {
    val execute by rememberUpdatedState(facade.execute) // reference 'execute' directly to enable lambda memoization
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = facade.selectedInputType,
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
            orderedInputTypes = facade.orderedInputTypes,
            selectedInputType = facade.selectedInputType,
            changeInputType = {
                val action = ColorInputGroupAction.ChangeInputType(it)
                execute(action)
            },
            strings = strings,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputSelector(
    orderedInputTypes: List<DomainColorInputType>,
    selectedInputType: DomainColorInputType,
    changeInputType: (DomainColorInputType) -> Unit,
    strings: ColorInputGroupUiStrings,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        orderedInputTypes.forEach { type ->
            val isSelected = (type == selectedInputType)
            val contentColor = LocalContentColor.current
            val colors = FilterChipDefaults.filterChipColors(
                labelColor = contentColor.copy(alpha = 0.60f),
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            val border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.60f),
                // selectedBorderColor doesn't matter because it has 0 width
            )
            FilterChip(
                selected = isSelected,
                onClick = { changeInputType(type) },
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
                facade = previewFacade(),
                strings = previewUiStrings(),
                hexInput = {
                    ColorInputHex(
                        facade = previewHexFacade(),
                        strings = previewHexUiStrings(),
                    )
                },
                rgbInput = {
                    ColorInputRgb(
                        facade = previewRgbFacade(),
                        strings = previewRgbUiStrings(),
                    )
                },
                hsvInput = {
                    ColorInputHsv(
                        facade = previewHsvFacade(),
                    )
                },
            )
        }
    }
}

private fun previewFacade() =
    ColorInputGroupFacade(
        execute = {},
        selectedInputType = DomainColorInputType.Hex,
        orderedInputTypes = listOf(
            DomainColorInputType.Hex,
            DomainColorInputType.Rgb,
            DomainColorInputType.Hsv,
        ),
    )

private fun previewUiStrings() =
    ColorInputGroupUiStrings(
        hexLabel = "HEX",
        rgbLabel = "RGB",
        hsvLabel = "HSV",
    )

private fun previewHexFacade() =
    ColorInputHexFacade(
        textField = TextFieldFacade(
            execute = {},
            text = TextFieldData.Text("1A803F") causedByUser true,
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = true,
            inputProcessor = TextFieldInputProcessor { TextFieldData.Text(it) },
        ),
        execute = {},
        inputSubmissionResult = null,
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

private fun previewRgbFacade() =
    ColorInputRgbFacade(
        rTextField = TextFieldFacade(
            execute = {},
            text = TextFieldData.Text("12") causedByUser true,
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = false,
            inputProcessor = TextFieldInputProcessor { TextFieldData.Text(it) },
        ),
        gTextField = TextFieldFacade(
            execute = {},
            text = TextFieldData.Text("") causedByUser true,
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = false,
            inputProcessor = TextFieldInputProcessor { TextFieldData.Text(it) },
        ),
        bTextField = TextFieldFacade(
            execute = {},
            text = TextFieldData.Text("255") causedByUser true,
            shouldSelectAllTextOnFocus = true,
            isClearTextFeatureEnabled = false,
            inputProcessor = TextFieldInputProcessor { TextFieldData.Text(it) },
        ),
        execute = {},
        inputSubmissionResult = null,
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

private fun previewHsvFacade() =
    ColorInputHsvFacade(
        execute = {},
        color = DomainColor.Hsv(hue = 117f, saturation = 0.59f, value = 0.31f),
    )