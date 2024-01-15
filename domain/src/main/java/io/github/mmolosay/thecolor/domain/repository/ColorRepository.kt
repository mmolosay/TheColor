package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest
import kotlinx.coroutines.flow.Flow

interface ColorRepository {
    suspend fun getColorDetails(colorHex: String): ColorDetails
    suspend fun getColorScheme(request: ColorSchemeRequest): ColorScheme
}