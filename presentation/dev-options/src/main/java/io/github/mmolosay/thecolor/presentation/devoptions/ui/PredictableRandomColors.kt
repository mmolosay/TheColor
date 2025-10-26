package io.github.mmolosay.thecolor.presentation.devoptions.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.impl.settings.SettingsItemUiComponents.AnimatedTextValue
import io.github.mmolosay.thecolor.presentation.impl.settings.SettingsItemUiComponents.ContentPadding
import io.github.mmolosay.thecolor.presentation.impl.settings.SettingsItemUiComponents.DefaultLabel
import io.github.mmolosay.thecolor.presentation.impl.settings.SettingsItemUiComponents.Description
import io.github.mmolosay.thecolor.presentation.impl.settings.SettingsItemUiComponents.TextValue
import io.github.mmolosay.thecolor.presentation.impl.settings.SettingsItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.impl.settings.SettingsItemUiComponents.ValueSpacing
import io.github.mmolosay.thecolor.presentation.impl.settings.SettingsItemUiComponents.animatedAttentionBadge
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
    Row(
        modifier = Modifier
            .selectable(
                selected = option.isSelected,
                onClick = option.onSelect,
                role = Role.RadioButton,
            )
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row {
            Text(
                modifier = Modifier.alignByBaseline(),
                text = option.name,
            )
            if (option.isDefault) {
                Spacer(Modifier.width(8.dp))
                DefaultLabel(
                    modifier = Modifier.alignByBaseline(),
                    text = option.defaultLabel,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        RadioButton(
            selected = option.isSelected,
            onClick = option.onSelect,
        )
    }
}

internal data class PredictableRandomColorsOption(
    val name: String,
    val isDefault: Boolean,
    val defaultLabel: String,
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
        val defaultLabel = "Default"
        val options = listOf(
            PredictableRandomColorsOption(
                name = "Random",
                isDefault = true,
                defaultLabel = defaultLabel,
                isSelected = true,
                onSelect = {},
            ),
            PredictableRandomColorsOption(
                name = "Cycling [Red, Green, Blue]",
                isDefault = false,
                defaultLabel = defaultLabel,
                isSelected = false,
                onSelect = {},
            ),
            PredictableRandomColorsOption(
                name = "Cycling [Light, Dark]",
                isDefault = false,
                defaultLabel = defaultLabel,
                isSelected = false,
                onSelect = {},
            ),
        )
        PredictableRandomColorsOptionSelection(
            options = options,
        )
    }
}