package io.github.mmolosay.thecolor.data.remote

import io.github.mmolosay.thecolor.data.remote.api.TheColorApiService
import io.github.mmolosay.thecolor.data.remote.mapper.ColorDetailsMapper
import io.github.mmolosay.thecolor.data.remote.mapper.ColorMapper
import io.github.mmolosay.thecolor.data.remote.mapper.ColorSchemeMapper
import io.github.mmolosay.thecolor.data.remote.mapper.toDto
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import javax.inject.Inject

/**
 * The implementation of [ColorRepository] that fetches data from remote server.
 */
class ColorRepositoryRemoteImpl @Inject constructor(
    private val api: TheColorApiService,
    private val colorConverter: ColorConverter,
    private val colorMapper: ColorMapper,
    private val colorDetailsMapper: ColorDetailsMapper,
    private val colorSchemeMapper: ColorSchemeMapper,
) : ColorRepository {

    override suspend fun lastSearchedColor(): Color.Abstract? =
        null

    override suspend fun getColorDetails(color: Color): ColorDetails {
        val string = color.toDtoString()
        val colorDetailsDto = api.getColorDetails(hex = string)
        return with(colorDetailsMapper) { colorDetailsDto.toDomain() }
    }

    override suspend fun getColorScheme(request: GetColorSchemeUseCase.Request): ColorScheme {
        val seedHex = request.seed.toDtoString()
        val colorSchemeDto = api.getColorScheme(
            hex = seedHex,
            mode = request.mode.toDto(),
            swatchCount = request.swatchCount,
        )
        return with(colorSchemeMapper) { colorSchemeDto.toDomain() }
    }

    private fun Color.toDtoString(): String {
        val hex = with(colorConverter) { toAbstract().toHex() }
        return with(colorMapper) { hex.toHexString() }
    }
}