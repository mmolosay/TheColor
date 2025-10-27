package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import io.github.mmolosay.thecolor.presentation.design.ColorScheme
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.common.compose.clipFullyRounded

/**
 * Structurally repeats contents and arrangement of [ColorInputRgb].
 */
@Composable
internal fun ColorInputRgbLoading(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.shimmer(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        @Suppress("NAME_SHADOWING")
        val modifier = Modifier.weight(1f)
        TextField(modifier)
        TextField(modifier)
        TextField(modifier)
    }
}

@Composable
private fun TextField(
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .height(64.dp) // height of 'ColorInputRgb's TextField() from Layout Inspector
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
            ColorInputRgbLoading()
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
            ColorInputRgbLoading()
        }
    }
}