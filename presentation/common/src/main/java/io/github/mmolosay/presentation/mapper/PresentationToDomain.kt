package io.github.mmolosay.presentation.mapper

import io.github.mmolosay.presentation.model.color.Color
import io.github.mmolosay.presentation.model.color.ColorPrototype
import io.github.mmolosay.presentation.model.color.data.ColorSchemeRequest
import io.github.mmolosay.presentation.model.color.from
import io.github.mmolosay.presentation.model.color.toHex
import com.ordolabs.domain.model.Color.Hex as ColorHexDomain
import com.ordolabs.domain.model.Color.Rgb as ColorRgbDomain
import com.ordolabs.domain.model.ColorSchemeRequest as ColorSchemeRequestDomain

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