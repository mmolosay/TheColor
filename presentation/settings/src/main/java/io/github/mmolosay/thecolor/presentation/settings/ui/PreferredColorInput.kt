package io.github.mmolosay.thecolor.presentation.settings.ui

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
import io.github.mmolosay.thecolor.presentation.settings.ui.ItemUiComponents.Description
import io.github.mmolosay.thecolor.presentation.settings.ui.ItemUiComponents.TextValue
import io.github.mmolosay.thecolor.presentation.settings.ui.ItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.settings.ui.UiComponents.DefaultItemContentPadding

@Composable
fun PreferredColorInput(
    title: String,
    description: String,
    selectedOption: String,
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
            Column {
                Title(text = title)
                Description(text = description)
            }

            Spacer(modifier = Modifier.width(32.dp)) // min
            Spacer(modifier = Modifier.weight(1f)) // max
            TextValue(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = selectedOption,
            )
        }
    }
}

@Composable
fun PreferredColorInputOptionSelection(
    options: List<ColorInputOption>,
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
    option: ColorInputOption,
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

data class ColorInputOption(
    val name: String,
    val isSelected: Boolean,
    val onSelect: () -> Unit,
)

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun PreferredColorInputPreview() {
    TheColorTheme {
        PreferredColorInput(
            title = "Preferred color input",
            description = "It will be selected on app startup.",
            selectedOption = "HEX",
            onClick = {},
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun PreferredColorInputSelectionPreview() {
    TheColorTheme {
        val options = listOf(
            ColorInputOption(
                name = "HEX",
                isSelected = true,
                onSelect = {},
            ),
            ColorInputOption(
                name = "RGB",
                isSelected = false,
                onSelect = {},
            ),
        )
        PreferredColorInputOptionSelection(
            options = options,
        )
    }
}