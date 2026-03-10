package io.github.mmolosay.thecolor.domain.color

interface ColorRepository {
    suspend fun getColorDetails(color: Color): Result<ColorDetails>
    suspend fun getColorScheme(request: GetColorSchemeRequest): Result<ColorScheme>

    data class GetColorSchemeRequest(
        val seed: Color,
        val mode: ColorScheme.Mode,
        val swatchCount: Int,
    )
}