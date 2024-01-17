package io.github.mmolosay.thecolor.presentation.mapper

import io.github.mmolosay.thecolor.presentation.color.Color
import io.github.mmolosay.thecolor.presentation.color.ColorPrototype
import io.github.mmolosay.thecolor.presentation.color.data.ColorSchemeRequest
import io.github.mmolosay.thecolor.presentation.color.from
import io.github.mmolosay.thecolor.presentation.color.toHex
import io.github.mmolosay.thecolor.domain.model.Color.Hex as ColorHexDomain
import io.github.mmolosay.thecolor.domain.model.Color.Rgb as ColorRgbDomain
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest as ColorSchemeRequestDomain

// TODO: replace with some ColorFactory that turns presentation ColorPrototype-s into domain Color-s
fun ColorPrototype.Hex.toDomainOrNull(): ColorHexDomain? {
    Color.from(this) ?: return null
    return ColorHexDomain(
        value = this.value!!, // checked in converting to valid color above
    )
}

fun ColorPrototype.Rgb.toDomainOrNull(): ColorRgbDomain? {
    Color.from(this) ?: return null
    return ColorRgbDomain(
        r = this.r!!, // checked in converting to valid color above
        g = this.g!!, // checked in converting to valid color above
        b = this.b!!, // checked in converting to valid color above
    )
}

fun ColorSchemeRequest.toDomain(): ColorSchemeRequestDomain =
    ColorSchemeRequestDomain(
        seedHex = this.seed.toHex().value!!,
        modeOrdinal = this.config.modeOrdinal,
        sampleCount = this.config.sampleCount,
    )