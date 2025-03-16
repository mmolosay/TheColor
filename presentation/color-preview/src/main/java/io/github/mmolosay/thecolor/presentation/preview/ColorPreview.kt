package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.toCompose

@Composable
@Suppress("unused") // good to have a high-level composable that accepts ViewModel
fun ColorPreview(
    viewModel: ColorPreviewViewModel,
) {
    ColorPreview(
        data = viewModel.dataFlow.collectAsStateWithLifecycle().value,
    )
}

@Composable
fun ColorPreview(
    data: ColorPreviewData,
) {
    val updates = remember { mutableStateListOf<UpdateOfDataWithColor>() }
    // we want to have last data WITH color memoized to show animation of scaling the preview down
    // once the new data WITHOUT color arrives
    var main by remember { mutableStateOf(data) }
    val scale by animateFloatAsState(
        targetValue = if (data.hasColor) 1f else 0f,
        label = "preview scale",
        finishedListener = { value ->
            // we need to keep last data WITH color until the preview is completely scaled down
            // and gone. Only after it the actual value can be set
            if (value == 0f) {
                main = data
            }
        },
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        main.let {
            if (it.hasColor) {
                Main(dataWithColor = it)
            }
        }

        updates.forEach { update ->
            // https://medium.com/@android-world/understanding-the-key-function-in-jetpack-compose-34accc92d567
            key(update) {
                UpdateRipple(
                    dataWithColor = update.dataWithColor,
                    onAnimationFinished = {
                        updates.remove(update)
                        main = update.dataWithColor
                    },
                )
            }
        }
    }

    LaunchedEffect(data) {
        val isAnUpdate = (data != main || updates.isNotEmpty())
        if (data.hasColor && isAnUpdate) {
            val id = updates.lastOrNull()?.id?.let { it + 1 } ?: 0
            val update = UpdateOfDataWithColor(data, id)
            updates.add(update)
        }
        if (data.hasNoColor) {
            updates.clear()
        }
    }
}

@Composable
private fun Main(
    dataWithColor: ColorPreviewData,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = CircleShape, // so shadow has the circular shape
        color = requireNotNull(dataWithColor.color).toCompose(),
        shadowElevation = 4.dp,
        content = {},
    )
}

@Composable
private fun UpdateRipple(
    dataWithColor: ColorPreviewData,
    onAnimationFinished: () -> Unit,
) {
    val scaleAnim = remember {
        Animatable(initialValue = 0f)
    }
    val color = requireNotNull(dataWithColor.color).toCompose()
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

private data class UpdateOfDataWithColor(
    val dataWithColor: ColorPreviewData,
    val id: Int,
)

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorPreview(
            data = previewData(),
        )
    }
}

private fun previewData() =
    ColorPreviewData(
        color = ColorInt(0x13264D),
    )