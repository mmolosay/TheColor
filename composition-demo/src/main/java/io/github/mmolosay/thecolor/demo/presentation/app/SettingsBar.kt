package io.github.mmolosay.thecolor.demo.presentation.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.demo.R
import io.github.mmolosay.thecolor.demo.data.FakeDemoSettings
import io.github.mmolosay.thecolor.demo.domain.DecimalSeparator

// A stand-in for the app's settings screen, which writes a setting while the recipe screen is open.
// It isn't part of the recipe screen, so it reads and writes the settings directly
@Composable
fun SettingsBar(
    settings: FakeDemoSettings,
    modifier: Modifier = Modifier,
) {
    val separator by settings.decimalSeparator.collectAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.settings_decimal_comma),
            style = MaterialTheme.typography.labelLarge,
        )
        Switch(
            checked = (separator == DecimalSeparator.Comma),
            onCheckedChange = { isChecked ->
                val new = if (isChecked) DecimalSeparator.Comma else DecimalSeparator.Point
                settings.setDecimalSeparator(new)
            },
            enabled = (separator != null), // not read yet
        )
    }
}
