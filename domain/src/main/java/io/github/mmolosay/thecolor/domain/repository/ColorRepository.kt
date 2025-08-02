package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.result.Result

interface ColorRepository {
    suspend fun getColorDetails(color: Color): Result<ColorDetails>
    suspend fun getColorScheme(request: GetColorSchemeRequest): Result<ColorScheme>

    data class GetColorSchemeRequest(
        val seed: Color,
        val mode: ColorScheme.Mode,
        val swatchCount: Int,
    )
}