package io.github.mmolosay.thecolor.presentation.impl

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