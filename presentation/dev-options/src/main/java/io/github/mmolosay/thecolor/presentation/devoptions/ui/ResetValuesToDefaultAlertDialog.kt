package io.github.mmolosay.thecolor.presentation.devoptions.ui

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
    strings: DevOptionsUiStrings,
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
            Text(text = strings.resetValuesToDefaultDialogTitle)
        },
        text = {
            Text(text = strings.resetValuesToDefaultDialogText)
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest,
                text = strings.resetValuesToDefaultDialogDismissButtonText,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmClick,
                text = strings.resetValuesToDefaultDialogConfirmButtonText,
            )
        },
    )
}