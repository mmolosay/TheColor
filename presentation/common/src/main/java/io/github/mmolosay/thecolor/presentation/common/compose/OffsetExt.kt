package io.github.mmolosay.thecolor.presentation.common.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/*
 * Utils and extensions for 'Offset' from Compose library.
 */

fun Offset.toDpOffset(density: Density): DpOffset =
    with(density) {
        DpOffset(x = x.toDp(), y = y.toDp())
    }

fun Offset.toIntOffset(): IntOffset =
    IntOffset(x = x.roundToInt(), y = y.roundToInt())

fun DpOffset.toPx(density: Density): Offset =
    Offset(
        x = with(density) { this@toPx.x.toPx() },
        y = with(density) { this@toPx.y.toPx() },
    )