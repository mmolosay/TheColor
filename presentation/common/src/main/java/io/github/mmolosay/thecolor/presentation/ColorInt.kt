package io.github.mmolosay.thecolor.presentation

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import javax.inject.Inject
import javax.inject.Singleton
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Valid color in RRGGBB format without alpha channel.
 *
 * Example: `0x1A803F`.
 */
@JvmInline
value class ColorInt(val hex: Int)

fun ColorInt.toCompose(): ComposeColor =
    ComposeColor(0xFF_000000 or this.hex.toLong())

/** Converts domain [Color] into [ColorInt]. */
@Singleton
class ColorToColorIntUseCase @Inject constructor(
    private val colorConverter: ColorConverter,
) {

    fun Color.toColorInt(): ColorInt {
        val hex = with(colorConverter) { toHex() }
        return ColorInt(hex = hex.value)
    }
}