package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel.State

@Composable
fun ColorPreview(
    vm: ColorPreviewViewModel,
) {
    val state = vm.stateFlow.collectAsStateWithLifecycle().value
    when (state) {
        is State.Absent -> {
            Spacer(modifier = Modifier.size(Size))
        }
        is State.Present -> {
            val uiData = ColorPreviewUiData(state.data)
            ColorPreview(uiData)
        }
    }
}

@Composable
fun ColorPreview(
    uiData: ColorPreviewUiData,
) {
    Box(
        modifier = Modifier
            .size(Size)
            .clip(CircleShape)
            .background(uiData.color),
    )
}

private val Size = 48.dp

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorPreview(uiData = previewUiData())
    }
}

private fun previewUiData() =
    ColorPreviewUiData(
        color = Color(0xFF_13264D),
    )