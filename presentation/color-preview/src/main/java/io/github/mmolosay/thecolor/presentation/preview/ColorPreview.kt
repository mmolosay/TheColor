package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel.State

@Composable
fun ColorPreview(
    vm: ColorPreviewViewModel,
) {
    val state = vm.stateFlow.collectAsStateWithLifecycle().value
    Box(
        modifier = Modifier.size(48.dp),
    ) {
        when (state) {
            is State.Absent -> Unit // show nothing
            is State.Present -> {
                val uiData = ColorPreviewUiData(state.data)
                ColorPreview(uiData)
            }
        }
    }
}

@Composable
fun ColorPreview(
    uiData: ColorPreviewUiData,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(uiData.color),
    )
}