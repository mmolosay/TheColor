package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState.Hidden
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState.Visible

@Composable
fun ColorPreview(
    vm: ColorPreviewViewModel,
) {
    val data = vm.stateFlow.collectAsStateWithLifecycle().value
    val uiState = ColorPreviewUiState(data)
    ColorPreview(uiState)
}

@Composable
fun ColorPreview(
    uiState: ColorPreviewUiState,
) {
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        val updates = remember { mutableStateListOf<VisibleStateUpdate>() }
        val resizingAlignment = Alignment.Center

        AnimatedVisibility(
            visible = uiState is Visible,
            modifier = Modifier.clip(CircleShape),
            enter = expandIn(expandFrom = resizingAlignment),
            exit = shrinkOut(shrinkTowards = resizingAlignment),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                var main by remember { mutableStateOf(uiState as Visible) }
                Main(visibleState = main)

                updates.forEach { update ->
                    // https://medium.com/@android-world/understanding-the-key-function-in-jetpack-compose-34accc92d567
                    key(update) {
                        UpdateRipple(
                            visibleState = update.uiState,
                            onAnimationFinished = {
                                updates.remove(update)
                                main = update.uiState
                            },
                        )
                    }
                }
                LaunchedEffect(uiState) {
                    val isAnUpdate = (uiState != main || updates.isNotEmpty())
                    if (uiState is Visible && isAnUpdate) {
                        val id = updates.lastOrNull()?.id?.let { it + 1 } ?: 0
                        val update = VisibleStateUpdate(uiState, id)
                        updates.add(update)
                    }
                    if (uiState is Hidden) {
                        updates.clear()
                    }
                }
            }
        }
    }
}

@Composable
private fun Main(
    visibleState: Visible,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = CircleShape, // so shadow has the circular shape // TODO: shadow clipping issue should be solved when this Composable is integrated inside whole Composable screen
        color = visibleState.color,
        shadowElevation = 4.dp,
        content = {},
    )
}

@Composable
private fun UpdateRipple(
    visibleState: Visible,
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
            .background(visibleState.color),
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

private data class VisibleStateUpdate(
    val uiState: Visible,
    val id: Int,
)

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorPreview(uiState = previewUiState())
    }
}

private fun previewUiState() =
    Visible(
        color = Color(0xFF_13264D),
    )