package io.github.mmolosay.thecolor.presentation.input.hex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import io.github.mmolosay.thecolor.presentation.common.compose.clipFullyRounded
import io.github.mmolosay.thecolor.presentation.design.ColorScheme
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

/**
 * Structurally repeats contents and arrangement of [ColorInputHex].
 */
@Composable
internal fun ColorInputHexLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.shimmer(),
        contentAlignment = Alignment.Center,
    ) {
        TextField()
    }
}

@Composable
private fun TextField() {
    Box(
        modifier = Modifier
            .height(64.dp) // height of 'ColorInputHex's TextField() from Layout Inspector
            .defaultMinSize(minWidth = 180.dp)
            .fillMaxWidth(fraction = 0.50f)
            .clipFullyRounded()
            .background(color = LocalContentColor.current.copy(alpha = 0.30f))
    )
}

@Preview
@Composable
private fun PreviewLight() {
    TheColorTheme(
        colorScheme = ColorScheme.Light,
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ColorInputHexLoading()
        }
    }
}

@Preview
@Composable
private fun PreviewDark() {
    TheColorTheme(
        colorScheme = ColorScheme.Dark,
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ColorInputHexLoading()
        }
    }
}