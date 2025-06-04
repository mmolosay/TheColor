package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.toCompose
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

/**
 * Simple 'Color Preview' Composable.
 * Does not animate [data] changes.
 */
@Composable
fun ColorPreview(
    data: ColorPreviewData,
) {
    ColorPreview(
        uiState = data.toUiState(),
    )
}

/**
 * Simple 'Color Preview' Composable.
 * Does not animate [uiState] changes.
 */
@Composable
fun ColorPreview(
    uiState: UiState,
) {
    when (uiState) {
        is UiState.Hidden -> return // nothing to compose
        is UiState.Visible -> {
            ColorPreviewBox {
                MainPreview(color = uiState.color.toCompose())
            }
        }
    }
}

/**
 * Animated 'Color Preview' Composable.
 * DOES animate [uiState] changes.
 */
@Composable
fun AnimatedColorPreview(
    uiState: UiState,
    onAnimationFinished: (UiState) -> Unit,
) {
    val updates = remember { mutableStateListOf<UpdateOfVisibleUiState>() }
    // we want to have last data WITH color memoized to show animation of scaling the preview down
    // once the new data WITHOUT color arrives
    var mainUiState by remember { mutableStateOf(uiState) }
    val scale by animateFloatAsState(
        targetValue = if (uiState is UiState.Visible) 1f else 0f,
        label = "preview scale",
        finishedListener = { value ->
            // we need to keep last data WITH color until the preview is completely scaled down
            // and gone. Only after it the actual value can be set
            if (value == 0f) {
                mainUiState = uiState
            }
            onAnimationFinished(uiState)
        },
    )

    ColorPreviewBox(
        modifier = Modifier.scale(scale),
    ) {
        mainUiState.let {
            if (it is UiState.Visible) {
                MainPreview(color = it.color.toCompose())
            }
        }

        updates.forEach { update ->
            // https://medium.com/@android-world/understanding-the-key-function-in-jetpack-compose-34accc92d567
            key(update) {
                UpdateRipple(
                    color = update.uiState.color.toCompose(),
                    onAnimationFinished = {
                        mainUiState = update.uiState
                        onAnimationFinished(update.uiState)
                        updates.remove(update)
                    },
                )
            }
        }
    }

    LaunchedEffect(uiState) {
        val isAnUpdate = (uiState != mainUiState || updates.isNotEmpty())
        if (uiState is UiState.Visible && isAnUpdate) {
            val id = updates.lastOrNull()?.id?.let { it + 1 } ?: 0
            val update = UpdateOfVisibleUiState(uiState, id)
            updates += update
        }
        if (uiState is UiState.Hidden) {
            updates.clear()
        }
    }
}

@Composable
private fun ColorPreviewBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun MainPreview(
    color: Color,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = CircleShape, // so shadow has the circular shape
        color = color,
        shadowElevation = 4.dp,
        content = {},
    )
}

@Composable
private fun UpdateRipple(
    color: Color,
    onAnimationFinished: () -> Unit,
) {
    val scaleAnim = remember {
        Animatable(initialValue = 0f)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scaleAnim.value)
            .clip(CircleShape)
            .background(color),
    )
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        )
        onAnimationFinished()
    }
}

private data class UpdateOfVisibleUiState(
    val uiState: UiState.Visible,
    val id: Int,
)

@Preview(showBackground = true)
@Composable
private fun NotAnimatedPreview() {
    TheColorTheme {
        ColorPreview(
            uiState = UiState.Visible(color = ColorInt(0x13264D)),
        )
    }
}

/*
 * LaunchedEffect() in default, "static" preview may not always work.
 * Launch this preview with "Start Interactive Mode".
 */
@Preview(showBackground = true)
@Composable
private fun AnimatedPreview() {
    TheColorTheme {
        var uiState by remember { mutableStateOf<UiState>(UiState.Hidden) }
        AnimatedColorPreview(
            uiState = uiState,
            onAnimationFinished = {},
        )
        LaunchedEffect(Unit) {
            while (true) {
                delay(2.seconds)
                uiState = UiState.Visible(color = ColorInt(0x13264D))
                delay(1.seconds)
                uiState = UiState.Hidden
                delay(1.seconds)
                uiState = UiState.Visible(color = ColorInt(0x180100))
                delay(1.seconds)
                uiState = UiState.Visible(color = ColorInt(0x2215A9))
                delay(1.seconds)
                uiState = UiState.Visible(color = ColorInt(0x246651))
                delay(1.seconds)
                uiState = UiState.Visible(color = ColorInt(0x6AE237))
                delay(1.seconds)
                uiState = UiState.Hidden
            }
        }
    }
}