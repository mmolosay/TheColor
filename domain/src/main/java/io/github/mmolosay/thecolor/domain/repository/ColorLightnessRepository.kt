package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.Color

interface ColorLightnessRepository {
    fun hslLightness(color: Color): Float
}