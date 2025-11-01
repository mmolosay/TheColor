package io.github.mmolosay.thecolor.presentation.devoptions.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.AnimatedTextValue
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ContentPadding
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Description
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.TextValue
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ValueSpacing
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.animatedAttentionBadge
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.DefaultLabel
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.OptionLayout
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@Composable
internal fun PredictableRandomColors(
    title: String,
    description: String,
    value: String,
    showAttentionBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
    ) {
        Row(
            modifier = modifier
                .animatedAttentionBadge(show = showAttentionBadge)
                .padding(ContentPadding)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Title(text = title)
                Description(text = description)
            }

            Spacer(modifier = Modifier.width(ValueSpacing))
            Box(
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                AnimatedTextValue(
                    targetValue = value,
                ) { targetValue ->
                    TextValue(
                        text = targetValue,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PredictableRandomColorsOptionSelection(
    options: List<PredictableRandomColorsOption>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.selectableGroup(),
    ) {
        options.forEach { option ->
            Option(option)
        }
    }
}

@Composable
private fun Option(
    option: PredictableRandomColorsOption,
) {
    OptionLayout(
        isSelected = option.isSelected,
        onSelect = option.onSelect,
    ) {
        Row {
            Text(
                modifier = Modifier.alignByBaseline(),
                text = option.text,
            )
            if (option.isDefault) {
                Spacer(Modifier.width(8.dp))
                DefaultLabel(
                    modifier = Modifier.alignByBaseline(),
                )
            }
        }
    }
}

internal data class PredictableRandomColorsOption(
    val text: String,
    val isDefault: Boolean,
    val isSelected: Boolean,
    val onSelect: () -> Unit,
)

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun PredictableRandomColorsPreview() {
    TheColorTheme {
        PredictableRandomColors(
            title = "Predictable random colors",
            description = "Change the strategy of how random colors are produced.",
            value = "Random",
            showAttentionBadge = true,
            onClick = {},
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun PredictableRandomColorsOptionSelectionPreview() {
    TheColorTheme {
        val options = listOf(
            PredictableRandomColorsOption(
                text = "Random",
                isDefault = true,
                isSelected = true,
                onSelect = {},
            ),
            PredictableRandomColorsOption(
                text = "Cycling [Red, Green, Blue]",
                isDefault = false,
                isSelected = false,
                onSelect = {},
            ),
            PredictableRandomColorsOption(
                text = "Cycling [Light, Dark]",
                isDefault = false,
                isSelected = false,
                onSelect = {},
            ),
        )
        PredictableRandomColorsOptionSelection(
            options = options,
        )
    }
}