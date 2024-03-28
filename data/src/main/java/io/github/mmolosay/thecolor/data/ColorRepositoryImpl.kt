package io.github.mmolosay.thecolor.data

import io.github.mmolosay.thecolor.data.remote.api.TheColorApiService
import io.github.mmolosay.thecolor.data.mapper.ColorDetailsMapper
import io.github.mmolosay.thecolor.data.mapper.ColorSchemeMapper
import io.github.mmolosay.thecolor.data.mapper.toDto
import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import javax.inject.Inject

class ColorRepositoryImpl @Inject constructor(
    private val api: TheColorApiService,
    private val colorConverter: ColorConverter,
    private val colorMapper: ColorMapper,
    private val colorDetailsMapper: ColorDetailsMapper,
    private val colorSchemeMapper: ColorSchemeMapper,
) : ColorRepository {

    override suspend fun lastSearchedColor(): Color.Abstract? =
        null

    override suspend fun getColorDetails(color: Color): ColorDetails {
        val hex = with(colorConverter) { color.toAbstract().toHex() }
        val string = with(colorMapper) { hex.toHexString() }
        val colorDetailsDto = api.getColorDetails(hex = string)
        return with(colorDetailsMapper) { colorDetailsDto.toDomain() }
    }

    override suspend fun getColorScheme(request: GetColorSchemeUseCase.Request): ColorScheme {
        val seed = with(colorConverter) { request.seed.toAbstract().toHex() }
        val seedHex = with(colorMapper) { seed.toHexString() }
        val colorSchemeDto = api.getColorScheme(
            hex = seedHex,
            mode = request.mode.toDto(),
            swatchCount = request.swatchCount,
        )
        return with(colorSchemeMapper) { colorSchemeDto.toDomain() }
    }
}