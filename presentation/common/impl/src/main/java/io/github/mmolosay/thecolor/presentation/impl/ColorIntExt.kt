package io.github.mmolosay.thecolor.presentation.impl

import io.github.mmolosay.thecolor.presentation.api.ColorInt
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Converts [ColorInt] to [Color][androidx.compose.ui.graphics.Color] from Jetpack Compose library.
 */
fun ColorInt.toCompose(): ComposeColor =
    ComposeColor(0xFF_000000 or this.hex.toLong())