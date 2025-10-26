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
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.AnimatedTextValue
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ContentPadding
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Description
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.TextValue
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ValueSpacing
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.attentionBadge
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.thenIf

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
                .thenIf(showAttentionBadge) {
                    attentionBadge()
                }
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
        Text(text = option.name)

        Spacer(modifier = Modifier.weight(1f))
        RadioButton(
            selected = option.isSelected,
            onClick = option.onSelect,
        )
    }
}

internal data class PredictableRandomColorsOption(
    val name: String,
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
                name = "Random",
                isSelected = true,
                onSelect = {},
            ),
            PredictableRandomColorsOption(
                name = "Cycling [Red, Green, Blue]",
                isSelected = false,
                onSelect = {},
            ),
            PredictableRandomColorsOption(
                name = "Cycling [Light, Dark]",
                isSelected = false,
                onSelect = {},
            ),
        )
        PredictableRandomColorsOptionSelection(
            options = options,
        )
    }
}