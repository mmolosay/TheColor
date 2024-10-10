package io.github.mmolosay.thecolor.presentation.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface

/**
 * Displays [content] on surface of [surfaceColor] and provides [contentColors] to it.
 *
 * @see ColorsOnTintedSurface
 */
@Composable
fun TintedSurface(
    surfaceColor: Color,
    contentColors: ColorsOnTintedSurface,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.background(surfaceColor),
    ) {
        ProvideColorsOnTintedSurface(colors = contentColors) {
            content()
        }
    }
}