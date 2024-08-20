package io.github.mmolosay.thecolor.presentation.api

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Valid color in RRGGBB format without alpha channel.
 *
 * Example: `0x1A803F`.
 */
/*
 * When writing unit tests and creating mock of ColorInt, make sure the mock is relaxed.
 * Value classes are replaced with values they're wrapping in runtime.
 */
@JvmInline
value class ColorInt(val hex: Int)

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