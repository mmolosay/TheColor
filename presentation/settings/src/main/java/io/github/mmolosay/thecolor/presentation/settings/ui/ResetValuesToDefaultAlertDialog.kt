package io.github.mmolosay.thecolor.presentation.settings.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
internal fun ResetValuesToDefaultAlertDialog(
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
                imageVector = ImageVector.vectorResource(DesignR.drawable.ic_reset_settings),
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