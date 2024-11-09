package io.github.mmolosay.thecolor.presentation.settings.ui

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
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.settings.ui.ItemUiComponents.AnimatedTextValue
import io.github.mmolosay.thecolor.presentation.settings.ui.ItemUiComponents.Description
import io.github.mmolosay.thecolor.presentation.settings.ui.ItemUiComponents.TextValue
import io.github.mmolosay.thecolor.presentation.settings.ui.ItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.settings.ui.UiComponents.DefaultItemContentPadding
import io.github.mmolosay.thecolor.presentation.settings.ui.UiComponents.DefaultItemValueSpacing

@Composable
internal fun AppUiColorScheme(
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
    ) {
        Row(
            modifier = modifier
                .padding(DefaultItemContentPadding)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Title(text = title)
                Description(text = description)
            }

            Spacer(modifier = Modifier.width(DefaultItemValueSpacing))
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
internal fun AppUiColorSchemeSelection(
    options: List<AppUiColorSchemeOption>,
) {
    Column(
        modifier = Modifier.selectableGroup(),
    ) {
        options.forEach { option ->
            Option(option)
        }
    }
}

@Composable
private fun Option(
    option: AppUiColorSchemeOption,
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

internal data class AppUiColorSchemeOption(
    val name: String,
    val isSelected: Boolean,
    val onSelect: () -> Unit,
)

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun AppUiColorSchemePreview() {
    TheColorTheme {
        AppUiColorScheme(
            title = "UI theme",
            description = "A color scheme of the application.",
            value = "Auto",
            onClick = {},
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun AppUiColorSchemeSelectionPreview() {
    TheColorTheme {
        val options = listOf(
            AppUiColorSchemeOption(
                name = "Light",
                isSelected = true,
                onSelect = {},
            ),
            AppUiColorSchemeOption(
                name = "Dark",
                isSelected = false,
                onSelect = {},
            ),
            AppUiColorSchemeOption(
                name = "Auto (follows system)",
                isSelected = false,
                onSelect = {},
            ),
        )
        AppUiColorSchemeSelection(
            options = options,
        )
    }
}