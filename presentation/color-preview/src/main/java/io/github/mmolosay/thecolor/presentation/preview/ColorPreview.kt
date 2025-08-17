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
import androidx.compose.runtime.remember
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
import kotlin.time.Duration.Companion.milliseconds
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
 *
 * [animController] has to be operated on the side of the caller.
 */
@Composable
fun AnimatedColorPreview(
    animController: ColorPreviewAnimController,
    onUiStateReached: (reached: UiState) -> Unit,
) {
    val updatesOfVisibleUiState = animController.updatesOfVisibleUiState

    fun scaleTargetValue(dest: AnimState.Visibility): Float =
        when (dest) {
            AnimState.Visibility.Collapsed -> 0f
            AnimState.Visibility.Expanded -> 1f
        }

    val scaleAnimatable = remember {
        val initialVisibility = animController.animateVisibilityCommand.dest.value
        Animatable(initialValue = scaleTargetValue(initialVisibility))
    }
    LaunchedEffect(Unit) {
        animController.flowOfAnimateVisibilityCommand.collect collect@{ command ->
            val visibilityDest = command.dest.value
            val targetValue = scaleTargetValue(visibilityDest)

            val isAlreadyInTargetState = (scaleAnimatable.value == targetValue)
            if (isAlreadyInTargetState) {
                return@collect // already in target state
            }

            val isAlreadyRunningTowardsTarget =
                (scaleAnimatable.isRunning && scaleAnimatable.targetValue == targetValue)
            if (isAlreadyRunningTowardsTarget) {
                return@collect // already animating towards target state
            }

            // run suspendable animation in a new coroutine to unblock collect() for the next emission
            launch {
                command.onAnimStarted()
                scaleAnimatable.animateTo(
                    targetValue = targetValue,
                )
                command.onAnimFinished()
            }
        }
    }
    LaunchedEffect(Unit) {
        animController.flowOfStableReachedUiState.collect { uiState ->
            onUiStateReached(uiState)
        }
    }

    ColorPreviewBox(
        modifier = Modifier.scale(scaleAnimatable.value),
    ) {
        val mainUiState = animController.mainUiState
        if (mainUiState is UiState.Visible) {
            MainPreview(color = mainUiState.color.toCompose())
        }
        updatesOfVisibleUiState.forEach { update ->
            // https://medium.com/@android-world/understanding-the-key-function-in-jetpack-compose-34accc92d567
            key(update) {
                UpdateRipple(
                    color = update.uiState.color.toCompose(),
                    onAnimationFinished = {
                        animController.onUpdateAnimFinished(update)
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
            onUiStateReached = {},
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
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x180100)))
                delay(200.milliseconds)
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x2215A9)))
                delay(200.milliseconds)
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x246651)))
                delay(200.milliseconds)
                animController.onNewUiState(UiState.Visible(color = ColorInt(0x6AE237)))
                delay(2.seconds)
                animController.onNewUiState(UiState.Hidden)
            }
        }
    }
}