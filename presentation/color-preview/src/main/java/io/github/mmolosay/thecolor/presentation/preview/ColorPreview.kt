package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState as AnimState
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
 */
@Composable
fun AnimatedColorPreview(
    animController: ColorPreviewAnimController,
    onAnimationFinished: (UiState) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val mainUiState = animController.mainUiState
    val updates = remember { mutableStateListOf<UpdateOfVisibleUiState>() }

    fun scaleTargetValue(dest: AnimState.Visibility): Float =
        when (dest) {
            AnimState.Visibility.Collapsed -> 0f
            AnimState.Visibility.Expanded -> 1f
        }

    val scaleAnimatable = remember {
        val initialVisibility = animController.visibilityDest.dest
        Animatable(initialValue = scaleTargetValue(initialVisibility))
    }
    LaunchedEffect(Unit) {
        animController.flowOfVisibilityDest.collect { visibility ->
            val targetValue = scaleTargetValue(visibility.dest)
            if (scaleAnimatable.value == targetValue) {
                if (visibility.cause == mainUiState) {
                    onAnimationFinished(visibility.cause)
                }
                return@collect // already in target state
            }
            if (scaleAnimatable.isRunning && scaleAnimatable.targetValue == targetValue) {
                return@collect // already animating to target state
            }
            coroutineScope.launch {
                scaleAnimatable.animateTo(
                    targetValue = targetValue,
                )
                animController.onVisibilityAnimFinished(reached = visibility)
                onAnimationFinished(visibility.cause)
                if (visibility.cause is UiState.Hidden) {
                    updates.clear()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        animController.flowOfVisibleUiStateUpdates.collect { visibleUiState ->
            val id = updates.lastOrNull()?.id?.let { it + 1 } ?: 0
            val update = UpdateOfVisibleUiState(visibleUiState, id)
            updates += update
        }
    }

    ColorPreviewBox(
        modifier = Modifier.scale(scaleAnimatable.value),
    ) {
        if (mainUiState is UiState.Visible) {
            MainPreview(color = mainUiState.color.toCompose())
        }
        updates.forEach { update ->
            // https://medium.com/@android-world/understanding-the-key-function-in-jetpack-compose-34accc92d567
            key(update) {
                UpdateRipple(
                    color = update.uiState.color.toCompose(),
                    onAnimationFinished = {
                        animController.onUpdateAnimFinished(update.uiState)
                        updates.remove(update).also { wasRemoved ->
                            require(wasRemoved) { "finished update wasn't in the list of the ongoing updates" }
                        }
                        // don't invoke a callback if collapsing
                        if (animController.latestUiState !is UiState.Hidden && !scaleAnimatable.isRunning) {
                            onAnimationFinished(update.uiState)
                        }
                    },
                )
            }
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
        val animController = remember {
            ColorPreviewAnimControllerImpl(uiState = UiState.Hidden)
        }
        AnimatedColorPreview(
            animController = animController,
            onAnimationFinished = {},
        )
        LaunchedEffect(Unit) {
            while (true) {
                delay(2.seconds)
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x13264D)))
                delay(1.seconds)
                animController.onNewUiState(UiState.Hidden)
                delay(1.seconds)
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x180100)))
                delay(1.seconds)
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x2215A9)))
                delay(1.seconds)
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x246651)))
                delay(1.seconds)
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x6AE237)))
                delay(1.seconds)
                animController.onNewUiState(UiState.Hidden)
            }
        }
    }
}