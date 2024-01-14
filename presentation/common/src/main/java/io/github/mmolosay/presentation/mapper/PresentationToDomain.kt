package io.github.mmolosay.presentation.mapper

import com.ordolabs.domain.model.ColorHex
import com.ordolabs.domain.model.ColorRgb
import io.github.mmolosay.presentation.model.color.Color
import io.github.mmolosay.presentation.model.color.ColorPrototype
import io.github.mmolosay.presentation.model.color.data.ColorSchemeRequest
import io.github.mmolosay.presentation.model.color.from
import io.github.mmolosay.presentation.model.color.toHex
import com.ordolabs.domain.model.ColorSchemeRequest as ColorSchemeRequestDomain

fun ColorPrototype.Hex.toDomain(): ColorHex? {
    Color.from(this) ?: return null
    return ColorHex(
        value = this.value!! // checked in converting to valid color above
    )
}

fun ColorPrototype.Rgb.toDomain(): ColorRgb? {
    Color.from(this) ?: return null
    return ColorRgb(
        r = this.r!!, // checked in converting to valid color above
        g = this.g!!, // checked in converting to valid color above
        b = this.b!! // checked in converting to valid color above
    )
}

fun ColorSchemeRequest.toDomain(): ColorSchemeRequestDomain {
    return ColorSchemeRequestDomain(
        seedHex = this.seed.toHex().value!!,
        modeOrdinal = this.config.modeOrdinal,
        sampleCount = this.config.sampleCount
    )
}