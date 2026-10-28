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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

@Composable
fun ColorInputGroup(
    feature: ColorInputGroupFeature,
    hexInput: @Composable () -> Unit,
    rgbInput: @Composable () -> Unit,
    hsvInput: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorInputGroupUiStrings(context) }
    val data = feature.viewData.collectAsStateWithLifecycle().value ?: return
    ColorInputGroup(
        modifier = modifier,
        data = data,
        strings = strings,
        onTypeSelect = feature::select,
        hexInput = hexInput,
        rgbInput = rgbInput,
        hsvInput = hsvInput,
    )
}

@Composable
fun ColorInputGroup(
    data: ColorInputGroupViewData,
    strings: ColorInputGroupUiStrings,
    onTypeSelect: (DomainColorInputType) -> Unit,
    hexInput: @Composable () -> Unit,
    rgbInput: @Composable () -> Unit,
    hsvInput: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = data.selectedType,
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
                    DomainColorInputType.Hex -> hexInput()
                    DomainColorInputType.Rgb -> rgbInput()
                    DomainColorInputType.Hsv -> hsvInput()
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        InputSelector(
            orderedInputTypes = data.orderedTypes,
            selectedInputType = data.selectedType,
            onTypeSelect = onTypeSelect,
            strings = strings,
        )
    }
}

@Composable
private fun InputSelector(
    orderedInputTypes: List<DomainColorInputType>,
    selectedInputType: DomainColorInputType,
    onTypeSelect: (DomainColorInputType) -> Unit,
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
                onClick = { onTypeSelect(type) },
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
    @Composable
    fun ColorInputPlaceholder(label: String) {
        Placeholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            Text(text = label)
        }
    }

    TheColorTheme {
        Surface {
            ColorInputGroup(
                data = previewViewData(),
                strings = previewUiStrings(),
                onTypeSelect = {},
                hexInput = {
                    ColorInputPlaceholder("HEX Color Input")
                },
                rgbInput = {
                    ColorInputPlaceholder("RGB Color Input")
                },
                hsvInput = {
                    ColorInputPlaceholder("HSV Color Input")
                },
            )
        }
    }
}

private fun previewViewData() =
    ColorInputGroupViewData(
        selectedType = DomainColorInputType.Hex,
        orderedTypes = listOf(
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