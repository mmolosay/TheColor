package io.github.mmolosay.thecolor.presentation.common.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.R
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.ContentText
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.DefaultLabel
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.DefaultLabelSpacing
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.OptionLayout
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsOptionUiComponents.OptionsListLayout
import io.github.mmolosay.thecolor.presentation.design.ColorScheme
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

/**
 * Common UI components for an option / list of options for a settings item.
 * For instance, settings item X can have multiple possible values A, B, or C, from which user can select.
 * Those values are called options.
 */
object SettingsOptionUiComponents {

    /**
     * Space between a text section and a 'Default' label.
     */
    val DefaultLabelSpacing = 32.dp

    /**
     * Main content of the option represented as a text.
     */
    @Composable
    fun ContentText(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        Text(
            modifier = modifier,
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }

    @Composable
    fun OptionLayout(
        isSelected: Boolean,
        onSelect: () -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable RowScope.() -> Unit,
    ) {
        Row(
            modifier = modifier
                .selectable(
                    selected = isSelected,
                    onClick = onSelect,
                    role = Role.RadioButton,
                )
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()

            Spacer(modifier = Modifier.weight(1f))
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
            )
        }
    }

    @Composable
    fun DefaultLabel(
        modifier: Modifier = Modifier,
        text: String = stringResource(R.string.settings_option_default_label),
    ) {
        Box(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(percent = 100),
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    @Composable
    fun OptionsListLayout(
        modifier: Modifier = Modifier,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        Column(
            modifier = modifier.selectableGroup(),
            content = content,
        )
    }
}

@Preview
@Composable
private fun Preview_Light() {
    TheColorTheme(
        colorScheme = ColorScheme.Light,
    ) {
        OptionsList()
    }
}

@Preview
@Composable
private fun Preview_Dark() {
    TheColorTheme(
        colorScheme = ColorScheme.Dark,
    ) {
        OptionsList()
    }
}

@Composable
private fun OptionsList() {
    @Composable
    fun Option(
        text: String,
        showDefaultLabel: Boolean,
        isSelected: Boolean,
    ) {
        OptionLayout(
            isSelected = isSelected,
            onSelect = {},
        ) {
            Row {
                ContentText(
                    modifier = Modifier.alignByBaseline(),
                    text = text,
                )
                if (showDefaultLabel) {
                    Spacer(Modifier.width(DefaultLabelSpacing))
                    DefaultLabel(
                        modifier = Modifier.alignByBaseline(),
                        text = "Default",
                    )
                }
            }
        }
    }

    Surface {
        OptionsListLayout {
            Option(
                text = "First option",
                showDefaultLabel = true,
                isSelected = false,
            )
            Option(
                text = "Second option",
                showDefaultLabel = false,
                isSelected = true,
            )
            Option(
                text = "Third option",
                showDefaultLabel = false,
                isSelected = false,
            )
        }
    }
}