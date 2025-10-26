package io.github.mmolosay.thecolor.presentation.impl.compose

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize

/*
 * Utils and extensions for 'Size' from Compose library.
 */

fun IntSize.toDpSize(density: Density): DpSize =
    with(density) {
        DpSize(width = width.toDp(), height = height.toDp())
    }

fun DpSize.toPx(density: Density): Size =
    Size(
        width = with(density) { this@toPx.width.toPx() },
        height = with(density) { this@toPx.height.toPx() },
    )