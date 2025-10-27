package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.common.colorint.toCompose
import kotlinx.coroutines.CoroutineScope
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

    var uiState by remember { mutableStateOf(animController.uiStateWithVisibility.uiState) }
    LaunchedEffect(Unit) {
        animController.flowOfUiState.collect { uiStateWithVisibility ->
            uiState = uiStateWithVisibility.uiState
        }
    }
    val updatesOfVisibleUiState = remember {
        mutableStateListOf<UpdateOfVisibleUiStateWithId>()
    }

    fun scaleTargetValue(dest: AnimState.Visibility): Float =
        when (dest) {
            AnimState.Visibility.Collapsed -> 0f
            AnimState.Visibility.Expanded -> 1f
        }
    val scaleAnimatable = remember {
        val initialVisibility = animController.uiStateWithVisibility.visibility
        Animatable(initialValue = scaleTargetValue(initialVisibility))
    }

    DisposableEffect(Unit) {
        val view = AnimControllerViewImpl(
            scaleTargetValue = ::scaleTargetValue,
            scaleAnimatable = scaleAnimatable,
            updateUiState = { newUiState -> uiState = newUiState },
            updatesOfVisibleUiState = updatesOfVisibleUiState,
            coroutineScope = coroutineScope,
        )
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
        val uiState = uiState
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
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        )
        onAnimationFinished()
    }
}

/**
 * Implementation of [ColorPreviewAnimController.View] for [AnimatedColorPreview] Composable.
 *
 * Relies on Compose-specific mechanisms to propagate changes to UI, like [Animatable]
 * and [SnapshotStateList].
 */
private class AnimControllerViewImpl(
    private val scaleTargetValue: (dest: AnimState.Visibility) -> Float,
    private val scaleAnimatable: Animatable<Float, AnimationVector1D>,
    private val updateUiState: (UiState) -> Unit,
    private val updatesOfVisibleUiState: SnapshotStateList<UpdateOfVisibleUiStateWithId>,
    private val coroutineScope: CoroutineScope,
) : ColorPreviewAnimController.View {

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
            // set uiState to the dest uiState right away so that it's visible during animation
            val isCollapsed = (scaleAnimatable.value == 0f)
            if (isCollapsed && dest.visibility == AnimState.Visibility.Expanded) {
                updateUiState(dest.uiState)
            }
            onAnimStarted()
            scaleAnimatable.animateTo(
                targetValue = targetValue,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
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
        val update = UpdateOfVisibleUiStateWithId(
            uiState = uiState,
            onAnimStarted = onAnimStarted,
            onAnimFinished = {
                updatesOfVisibleUiState.removeAll { it.id == id }
                onAnimFinished()
            },
            id = id,
        )
        updatesOfVisibleUiState += update
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
            ColorPreviewAnimController(uiState = UiState.Hidden)
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