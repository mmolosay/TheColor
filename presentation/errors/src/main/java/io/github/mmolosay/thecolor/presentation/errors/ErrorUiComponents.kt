package io.github.mmolosay.thecolor.presentation.errors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable UI elements for a error View of varying appearance.
 */
object ErrorUiComponents {

    @Composable
    fun ErrorLayout(
        modifier: Modifier = Modifier,
        message: @Composable () -> Unit,
        action: (@Composable () -> Unit)?,
    ) {
        Column(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            message()
            action?.invoke()
        }
    }

    @Composable
    fun Message(
        text: String,
    ) {
        Text(
            text = text,
        )
    }

    @Composable
    fun ActionButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Button(
            modifier = modifier,
            onClick = onClick,
        ) {
            Text(
                text = text,
            )
        }
    }
}