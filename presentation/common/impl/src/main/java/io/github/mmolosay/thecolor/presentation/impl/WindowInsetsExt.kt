package io.github.mmolosay.thecolor.presentation.impl

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only

/*
 * Utils and extensions for 'WindowInsets'.
 */

/**
 * Returns a [WindowInsets] without only [WindowInsetsSides.Bottom] side.
 */
fun WindowInsets.withoutBottom(): WindowInsets {
    val allSidesWithoutBottom = WindowInsetsSides.Horizontal + WindowInsetsSides.Top
    return this.only(allSidesWithoutBottom)
}

/**
 * Returns a [WindowInsets] with _only_ [WindowInsetsSides.Bottom] side.
 */
fun WindowInsets.onlyBottom(): WindowInsets =
    this.only(WindowInsetsSides.Bottom)