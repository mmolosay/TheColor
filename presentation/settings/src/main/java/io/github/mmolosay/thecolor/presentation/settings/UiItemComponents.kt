package io.github.mmolosay.thecolor.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Reusable UI components for individual items on Settings screen.
 */
internal object UiItemComponents {

    @Composable
    fun ItemLayout(
        title: @Composable () -> Unit,
        description: (@Composable () -> Unit)?,
        valueContent: (@Composable () -> Unit)?,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier,
        ) {
            Column {
                title()
                description?.invoke()
            }
            valueContent?.invoke()
        }
    }

    @Composable
    fun Title(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        Text(
            modifier = modifier,
            text = text,
            style = MaterialTheme.typography.titleMedium,
        )
    }

    @Composable
    fun Description(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        Text(
            modifier = modifier,
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    @Composable
    fun TextValue(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        Text(
            modifier = modifier,
            text = text,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}