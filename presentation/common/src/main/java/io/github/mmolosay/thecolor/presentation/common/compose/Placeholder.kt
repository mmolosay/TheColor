package io.github.mmolosay.thecolor.presentation.common.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@Composable
fun Placeholder(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(all = 8.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .hazardStripes(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .drawWithContent {
                    drawRect(
                        color = Color.Black,
                        blendMode = BlendMode.DstOut,
                    )
                    drawContent()
                }
                .padding(contentPadding),
        ) {
            content()
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface {
            Placeholder(
                modifier = Modifier
                    .width(160.dp)
                    .height(90.dp),
            ) {
                Text(
                    text = "Label",
                )
            }
        }
    }
}