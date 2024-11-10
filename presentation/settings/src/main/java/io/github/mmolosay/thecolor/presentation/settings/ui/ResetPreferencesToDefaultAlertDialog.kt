package io.github.mmolosay.thecolor.presentation.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import io.github.mmolosay.thecolor.presentation.settings.SettingsUiStrings

@Composable
internal fun ResetPreferencesToDefaultAlertDialog(
    strings: SettingsUiStrings,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    @Composable
    fun Button(
        onClick: () -> Unit,
        text: String,
    ) {
        TextButton(
            onClick = onClick,
        ) {
            Text(text)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Outlined.RestartAlt,
                contentDescription = null, // purely decorative
            )
        },
        title = {
            Text(text = strings.resetPreferencesToDefaultDialogTitle)
        },
        text = {
            Text(text = strings.resetPreferencesToDefaultDialogText)
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest,
                text = strings.resetPreferencesToDefaultDialogDismissButtonText,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmClick,
                text = strings.resetPreferencesToDefaultDialogConfirmButtonText,
            )
        },
    )
}