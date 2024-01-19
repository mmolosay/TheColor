package io.github.mmolosay.thecolor.presentation.mapper

import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest
import io.github.mmolosay.thecolor.presentation.color.from
import io.github.mmolosay.thecolor.presentation.color.toHex
import io.github.mmolosay.thecolor.presentation.color.toRgb
import io.github.mmolosay.thecolor.domain.model.Color as ColorDomain
import io.github.mmolosay.thecolor.domain.model.ColorPrototype as ColorPrototypeDomain
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest as ColorSchemeRequestDomain

// TODO: replace with some ColorFactory that turns presentation ColorPrototype-s into domain Color-s
fun ColorPrototype.Hex.toDomainOrNull(): ColorDomain.Hex? {
    val color = Color.from(this)?.toHex() ?: return null
    return ColorDomain.Hex(
        value = color.value!!.toInt(radix = 16), // checked in converting to valid color above
    )
}

fun ColorPrototype.Rgb.toDomainOrNull(): ColorDomain.Rgb? {
    val color = Color.from(this)?.toRgb() ?: return null
    return ColorDomain.Rgb(
        r = color.r!!, // checked in converting to valid color above
        g = color.g!!, // checked in converting to valid color above
        b = color.b!!, // checked in converting to valid color above
    )
}

fun ColorInput.toDomain(): ColorPrototypeDomain =
    when (this) {
        is ColorInput.Hex -> this.toDomain()
        is ColorInput.Rgb -> this.toDomain()
    }

fun ColorInput.Hex.toDomain(): ColorPrototypeDomain.Hex =
    ColorPrototypeDomain.Hex(
        value = this.string.toIntOrNull(radix = 16),
    )

fun ColorInput.Rgb.toDomain(): ColorPrototypeDomain.Rgb =
    ColorPrototypeDomain.Rgb(
        r = this.r.toIntOrNull(),
        g = this.g.toIntOrNull(),
        b = this.b.toIntOrNull(),
    )

fun ColorSchemeRequest.toDomain(): ColorSchemeRequestDomain =
    ColorSchemeRequestDomain(
        seedHex = this.seed.toHex().value!!,
        modeOrdinal = this.config.modeOrdinal,
        sampleCount = this.config.sampleCount,
    )