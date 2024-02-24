package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

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
        // when preview is Hidden, we want to have memoized Visible data for some time while "exit" animation is running
        var visibleState by remember { mutableStateOf<ColorPreviewUiState.Visible?>(null) }
        val resizingAlignment = Alignment.Center
        AnimatedVisibility(
            visible = uiState is ColorPreviewUiState.Visible,
            modifier = Modifier.clip(CircleShape),
            enter = expandIn(expandFrom = resizingAlignment),
            exit = shrinkOut(shrinkTowards = resizingAlignment),
        ) {
            val lastVisible = visibleState ?: return@AnimatedVisibility
            Box(
                modifier = Modifier
                    .aspectRatio(1f) // square
                    .background(lastVisible.color),
            )
        }
        LaunchedEffect(uiState) {
            visibleState = uiState as? ColorPreviewUiState.Visible ?: return@LaunchedEffect
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorPreview(uiState = previewUiState())
    }
}

private fun previewUiState() =
    ColorPreviewUiState.Visible(
        color = Color(0xFF_13264D),
    )