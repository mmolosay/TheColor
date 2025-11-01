package io.github.mmolosay.thecolor.presentation.common.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset

/*
 * Utils and extensions for 'Offset' from Compose library.
 */

fun Offset.toDpOffset(density: Density): DpOffset =
    with(density) {
        DpOffset(x = x.toDp(), y = y.toDp())
    }

fun DpOffset.toPx(density: Density): Offset =
    Offset(
        x = with(density) { this@toPx.x.toPx() },
        y = with(density) { this@toPx.y.toPx() },
    )