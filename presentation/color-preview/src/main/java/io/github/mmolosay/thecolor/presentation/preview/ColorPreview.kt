package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
 * Animated 'Color Preview' Composable.
 *
 * [animController] has to be operated on the side of the caller.
 */
@Composable
fun AnimatedColorPreview(
    animController: ColorPreviewAnimController,
    onUiStateReached: (reached: UiState) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val updatesOfVisibleUiState = remember {
        mutableStateListOf<UpdateOfVisibleUiStateWithId>()
    }

    fun scaleTargetValue(dest: AnimState.Visibility): Float =
        when (dest) {
            AnimState.Visibility.Collapsed -> 0f
            AnimState.Visibility.Expanded -> 1f
        }

    val scaleAnimatable = remember {
        val initialVisibility = animController.flowOfUiState.value.visibility
        Animatable(initialValue = scaleTargetValue(initialVisibility))
    }

    val view = remember {
        object : ColorPreviewAnimController.View {
            override fun animateVisibility(
                dest: ColorPreviewAnimController.UiStateWithVisibility,
                onAnimStarted: () -> Unit,
                onAnimFinished: () -> Unit,
            ) {
                val targetValue = scaleTargetValue(dest.visibility)

                val isAlreadyInTargetState = (scaleAnimatable.value == targetValue)
                if (isAlreadyInTargetState) {
                    return // already in target state
                }

                val isAlreadyRunningTowardsTarget =
                    (scaleAnimatable.isRunning && scaleAnimatable.targetValue == targetValue)
                if (isAlreadyRunningTowardsTarget) {
                    return // already animating towards target state
                }

                // no need to manually cancel previous animation: Animatable.animateTo() will handle this
                coroutineScope.launch {
                    onAnimStarted()
                    scaleAnimatable.animateTo(
                        targetValue = targetValue,
                        animationSpec = tween(3000), // TODO: rollback
                    )
                    onAnimFinished()
                }
            }

            override fun animateUpdateOfVisibleUiState(
                uiState: UiState.Visible,
                onAnimStarted: () -> Unit,
                onAnimFinished: () -> Unit,
            ) {
                val id = updatesOfVisibleUiState.lastOrNull()?.id?.let { it + 1 } ?: 0
                val update = UpdateOfVisibleUiStateWithId(uiState, onAnimStarted, onAnimFinished, id)
                updatesOfVisibleUiState += update
            }
        }
    }

    DisposableEffect(Unit) {
        animController.setView(view)
        onDispose {
            animController.setView(null)
        }
    }
    LaunchedEffect(Unit) {
        animController.flowOfStableReachedUiState.collect { uiState ->
            onUiStateReached(uiState)
        }
    }

    Box(
        modifier = Modifier
            .scale(scaleAnimatable.value)
            .size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        val uiState = animController.flowOfUiState.collectAsStateWithLifecycle().value.uiState
        if (uiState is UiState.Visible) {
            Preview(color = uiState.color.toCompose())
        }
        updatesOfVisibleUiState.forEach { update ->
            // https://medium.com/@android-world/understanding-the-key-function-in-jetpack-compose-34accc92d567
            key(update) {
                UpdateRipple(
                    color = update.uiState.color.toCompose(),
                    onAnimationStarted = update.onAnimStarted,
                    onAnimationFinished = update.onAnimFinished,
                )
            }
        }
    }
}

@Composable
private fun Preview(
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
    onAnimationStarted: () -> Unit,
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
        onAnimationStarted()
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(800), // TODO: rollback
//            animationSpec = spring(
//                stiffness = Spring.StiffnessMediumLow,
//                visibilityThreshold = Spring.DefaultDisplacementThreshold,
//            ),
        )
        onAnimationFinished()
    }
}

/**
 * Couples update of [UiState.Visible] with [id].
 * The [id] is needed to run animations correctly.
 * Without [id], the "key" for animation will be only [UiState].
 * If there are two same [UiState]s currently animating, without [id] animation will break.
 * See [androidx.compose.runtime.key] function usage in this file.
 **/
private data class UpdateOfVisibleUiStateWithId(
    val uiState: UiState.Visible,
    val onAnimStarted: () -> Unit,
    val onAnimFinished: () -> Unit,
    val id: Int,
)

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