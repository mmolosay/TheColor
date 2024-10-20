package io.github.mmolosay.thecolor.presentation.settings.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Reusable UI components for individual items on Settings screen.
 */
internal object ItemUiComponents {

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