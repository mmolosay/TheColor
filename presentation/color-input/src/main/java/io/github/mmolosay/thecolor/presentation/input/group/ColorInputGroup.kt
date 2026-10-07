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
import androidx.compose.foundation.layout.wrapContentWidth
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
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHex
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacade
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsv
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFacade
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgb
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacade
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldInputProcessor
import kotlinx.coroutines.Job
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@Composable
fun ColorInputGroup(
    handle: ColorInputGroupHandle,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputGroupUiStrings(context) }
    val facade = rememberColorInputGroupFacade(handle)
    ColorInputGroup(
        modifier = modifier,
        facade = facade,
        strings = strings,
    )
}

@Composable
fun rememberColorInputGroupFacade(handle: ColorInputGroupHandle): ColorInputGroupFacade {
    val data = handle.dataFlow.collectAsStateWithLifecycle().value
    return remember(handle, data) { handle.facade(data) }
}

@Composable
fun ColorInputGroup(
    facade: ColorInputGroupFacade,
    strings: ColorInputGroupUiStrings,
    modifier: Modifier = Modifier,
) {
    val execute by rememberUpdatedState(facade.execute) // reference 'execute' directly to enable lambda memoization
    Column(
        modifier = modifier,
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
                    .wrapContentWidth(Alignment.CenterHorizontally),
            ) {
                when (type) {
                    DomainColorInputType.Hex ->
                        ColorInputHex(facade = facade.hex)
                    DomainColorInputType.Rgb ->
                        ColorInputRgb(facade = facade.rgb)
                    DomainColorInputType.Hsv ->
                        ColorInputHsv(facade = facade.hsv)
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
            )
        }
    }
}

private fun previewFacade() =
    ColorInputGroupFacade(
        hex = ColorInputHexFacade(
            textField = TextFieldFacade(
                text = TextFieldData.Text("1A803F"),
                shouldSelectAllTextOnFocus = true,
                isClearTextFeatureEnabled = true,
                inputProcessor = TextFieldInputProcessor { TextFieldData.Text(it) },
                execute = { Job() },
            ),
            inputSubmissionResult = null,
            execute = { Job() },
        ),
        rgb = ColorInputRgbFacade(
            rTextField = TextFieldFacade(
                text = TextFieldData.Text("12"),
                shouldSelectAllTextOnFocus = true,
                isClearTextFeatureEnabled = false,
                inputProcessor = TextFieldInputProcessor { TextFieldData.Text(it) },
                execute = { Job() },
            ),
            gTextField = TextFieldFacade(
                text = TextFieldData.Text(""),
                shouldSelectAllTextOnFocus = true,
                isClearTextFeatureEnabled = false,
                inputProcessor = TextFieldInputProcessor { TextFieldData.Text(it) },
                execute = { Job() },
            ),
            bTextField = TextFieldFacade(
                text = TextFieldData.Text("255"),
                shouldSelectAllTextOnFocus = true,
                isClearTextFeatureEnabled = false,
                inputProcessor = TextFieldInputProcessor { TextFieldData.Text(it) },
                execute = { Job() },
            ),
            inputSubmissionResult = null,
            isSmartBackspaceEnabled = true,
            execute = { Job() },
        ),
        hsv = ColorInputHsvFacade(
            color = Color.Hsv(hue = 117f, saturation = 0.59f, value = 0.31f),
            execute = { Job() },
        ),
        selectedInputType = DomainColorInputType.Hex,
        orderedInputTypes = listOf(
            DomainColorInputType.Hex,
            DomainColorInputType.Rgb,
            DomainColorInputType.Hsv,
        ),
        execute = { Job() },
    )

private fun previewUiStrings() =
    ColorInputGroupUiStrings(
        hexLabel = "HEX",
        rgbLabel = "RGB",
        hsvLabel = "HSV",
    )