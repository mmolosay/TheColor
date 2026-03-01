package io.github.mmolosay.thecolor.presentation.input.hsv

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

/**
 * Structurally repeats contents and arrangement of [ColorInputHsv].
 */
@Composable
internal fun ColorInputHsvLoading(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .shimmer(),
    ) {
        Box(
            modifier = Modifier
                .width(128.dp)
                .aspectRatio(1f / 1f)
                .clip(RoundedCornerShape(size = 4.dp))
                .background(color = LocalContentColor.current.copy(alpha = 0.30f)),
        )

        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp) // difference between thumb and track width
                .width(16.dp) // width of the track
                .fillMaxHeight()
                .clip(RoundedCornerShape(size = 4.dp))
                .background(color = LocalContentColor.current.copy(alpha = 0.30f)),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ColorInputHsvLoading()
        }
    }
}