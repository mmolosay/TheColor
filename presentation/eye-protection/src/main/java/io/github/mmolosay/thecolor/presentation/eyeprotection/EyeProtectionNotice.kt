package io.github.mmolosay.thecolor.presentation.eyeprotection

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.ColorScheme
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@Composable
fun EyeProtectionNotice(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Filled.Visibility,
            contentDescription = null, // purely decorative,
        )

        Spacer(Modifier.width(8.dp))
        Text(
            text = "Eye protection is active.", // TODO: use real string
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview
@Composable
private fun PreviewLight() {
    TheColorTheme(
        colorScheme = ColorScheme.Light,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            EyeProtectionNotice()
        }
    }
}

@Preview
@Composable
private fun PreviewDark() {
    TheColorTheme(
        colorScheme = ColorScheme.Dark,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            EyeProtectionNotice()
        }
    }
}