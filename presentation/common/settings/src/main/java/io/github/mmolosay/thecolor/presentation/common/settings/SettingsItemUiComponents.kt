package io.github.mmolosay.thecolor.presentation.common.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.AnimatedTextValue
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ContentPadding
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Description
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.TextValue
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.Title
import io.github.mmolosay.thecolor.presentation.common.settings.SettingsItemUiComponents.ValueSpacing
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

/**
 * Common UI components for an individual item in a list of settings.
 */
object SettingsItemUiComponents {

    /**
     * Space between the border of the item and the content inside of it.
     */
    val ContentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

    /**
     * Space between a text section and a value inside a settings item.
     */
    val ValueSpacing = 32.dp

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

    @Composable
    fun AnimatedTextValue(
        targetValue: String,
        content: @Composable AnimatedContentScope.(targetState: String) -> Unit,
    ) =
        AnimatedContent(
            targetState = targetValue,
            transitionSpec = {
                fun <T> animationSpec() = tween<T>(durationMillis = 400)
                val enter = kotlin.run {
                    val slideIn = slideInVertically(animationSpec()) { height -> -height }
                    slideIn + fadeIn(animationSpec())
                }
                val exit = kotlin.run {
                    val slideOut = slideOutVertically(animationSpec()) { height -> height }
                    slideOut + fadeOut(animationSpec())
                }
                (enter togetherWith exit)
                    .using(SizeTransform(clip = false))
            },
            contentAlignment = Alignment.CenterEnd,
            label = "animated content of text value",
            content = content,
        )
}

@Preview
@Composable
private fun Preview() {
    TheColorTheme {
        val values = listOf("First value", "Second", "Third (3rd) value")
        var indexOfNextValue by remember { mutableIntStateOf(0) }
        val value = values[indexOfNextValue]

        Surface(
            onClick = { indexOfNextValue = (indexOfNextValue + 1) % values.size },
        ) {
            Row(
                modifier = Modifier
                    .padding(ContentPadding)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Title(text = "Title of the item")
                    Description(text = "Verbose description of the item. May span for multiple lines.")
                }

                Spacer(modifier = Modifier.width(ValueSpacing))
                Box(
                    modifier = Modifier.align(Alignment.CenterVertically),
                ) {
                    AnimatedTextValue(
                        targetValue = value,
                    ) { targetValue ->
                        TextValue(
                            text = targetValue,
                        )
                    }
                }
            }
        }
    }
}