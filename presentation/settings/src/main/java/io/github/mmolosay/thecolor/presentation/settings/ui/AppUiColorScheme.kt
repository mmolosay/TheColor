package io.github.mmolosay.thecolor.presentation.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.AnimatedTextValue
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ContentPadding
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Description
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.TextValue
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ValueSpacing
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.ContentText
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.OptionLayout
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.OptionsListLayout
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

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
            AnimatedTextValue(
                modifier = Modifier.align(Alignment.CenterVertically),
                targetValue = value,
            ) { targetValue ->
                TextValue(
                    text = targetValue,
                )
            }
        }
    }
}

@Composable
internal fun AppUiColorSchemeSelection(
    options: List<AppUiColorSchemeOption>,
    modifier: Modifier = Modifier,
) {
    OptionsListLayout(
        modifier = modifier,
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
    OptionLayout(
        isSelected = option.isSelected,
        onSelect = option.onSelect,
    ) {
        ContentText(
            text = option.text,
        )
    }
}

internal data class AppUiColorSchemeOption(
    val text: String,
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
                text = "Light",
                isSelected = true,
                onSelect = {},
            ),
            AppUiColorSchemeOption(
                text = "Dark",
                isSelected = false,
                onSelect = {},
            ),
            AppUiColorSchemeOption(
                text = "Auto (follows system)",
                isSelected = false,
                onSelect = {},
            ),
        )
        AppUiColorSchemeSelection(
            options = options,
        )
    }
}