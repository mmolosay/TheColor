package io.github.mmolosay.thecolor.presentation.impl

import io.github.mmolosay.thecolor.presentation.api.ColorInt
import android.graphics.Color as AndroidColor
import androidx.annotation.ColorInt as AndroidColorInt
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Converts [ColorInt] to [Color][androidx.compose.ui.graphics.Color] from Jetpack Compose library.
 */
fun ColorInt.toCompose(): ComposeColor =
    ComposeColor(0xFF_000000 or this.hex.toLong())

/**
 * Converts [ColorInt] to color [Int] in `ARGB` format that is commonly used across Android SDK.
 */
@AndroidColorInt
fun ColorInt.toArgb(): Int =
    AndroidColor.BLACK or hex // 0xFF_000000 literal is treated as Long by Kotlin compiler