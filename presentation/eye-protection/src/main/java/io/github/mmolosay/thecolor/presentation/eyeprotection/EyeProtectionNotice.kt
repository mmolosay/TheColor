package io.github.mmolosay.thecolor.presentation.eyeprotection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(size = 8.dp),
        color = MaterialTheme.colorScheme.surface, // TODO: use provided color
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant), // TODO: use provided color
    ) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Visibility,
                contentDescription = null, // purely decorative,
            )

            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Eye protection is active",
                    style = MaterialTheme.typography.labelMedium,
                )
                // TODO: show original color as little circle preview?
                Text(
                    text = "Color #ABCDEF has been dimmed.",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
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