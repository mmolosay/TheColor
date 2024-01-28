package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest

interface ColorRepository {
    suspend fun lastSearchedColor(): Color.Abstract?
    suspend fun getColorDetails(colorHex: String): ColorDetails
    suspend fun getColorDetails(color: Color): ColorDetails
    suspend fun getColorScheme(request: ColorSchemeRequest): ColorScheme
}