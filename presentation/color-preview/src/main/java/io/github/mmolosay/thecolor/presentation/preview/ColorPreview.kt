package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    val uiData = ColorPreviewUiData(data)
    ColorPreview(uiData)
}

@Composable
fun ColorPreview(
    uiData: ColorPreviewUiData,
) {
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        // when preview is Hidden, we want to have memoized Visible data for some time while "exit" animation is running
        var visiblePreview by remember { mutableStateOf<ColorPreviewUiData.Preview.Visible?>(null) }
        val resizingAlignment = Alignment.Center
        AnimatedVisibility(
            visible = uiData.preview is ColorPreviewUiData.Preview.Visible,
            modifier = Modifier.clip(CircleShape),
            enter = fadeIn() + expandIn(expandFrom = resizingAlignment),
            exit = fadeOut() + shrinkOut(shrinkTowards = resizingAlignment),
        ) {
            val lastVisible = visiblePreview ?: return@AnimatedVisibility
            Box(
                modifier = Modifier
                    .aspectRatio(1f) // square
                    .background(lastVisible.color),
            )
        }
        LaunchedEffect(uiData) {
            visiblePreview = uiData.preview as? ColorPreviewUiData.Preview.Visible
                ?: return@LaunchedEffect
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorPreview(uiData = previewUiData())
    }
}

private fun previewUiData() =
    ColorPreviewUiData(
        preview = ColorPreviewUiData.Preview.Visible(color = Color(0xFF_13264D)),
    )