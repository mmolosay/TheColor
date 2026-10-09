package io.github.mmolosay.thecolor.presentation.scheme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.colorint.toCompose
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeData

@Composable
internal fun Swatch(
    swatch: ColorSchemeData.Swatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors =
        rememberContentColors(useLight = swatch.isDark) // light content on dark and vice versa
    ProvideColorsOnTintedSurface(colors) { // provides correct ripple
        Box(
            modifier = modifier
                .size(SwatchSize)
                .clip(CircleShape)
                .background(swatch.color.toCompose())
                .clickable(onClick = onClick),
        )
    }
}

internal val SwatchSize = 80.dp