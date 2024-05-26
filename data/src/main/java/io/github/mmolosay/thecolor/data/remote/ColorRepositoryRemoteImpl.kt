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
import io.github.mmolosay.thecolor.domain.result.Result
import io.github.mmolosay.thecolor.domain.result.ResultMapper
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
    private val resultMapper: ResultMapper,
) : ColorRepository {

    override suspend fun lastSearchedColor(): Color.Abstract? =
        null

    override suspend fun getColorDetails(color: Color): Result<ColorDetails> {
        val colorString = color.toDtoString()
        val kotlinResult = runCatching {
            api.getColorDetails(hex = colorString)
        }
            .map { colorDetailsDto ->
                with(colorDetailsMapper) { colorDetailsDto.toDomain() }
            }
        return with(resultMapper) { kotlinResult.toDomainResult() }
    }

    override suspend fun getColorScheme(request: GetColorSchemeUseCase.Request): Result<ColorScheme> {
        val seedHex = request.seed.toDtoString()
        val kotlinResult = runCatching {
            api.getColorScheme(
                hex = seedHex,
                mode = request.mode.toDto(),
                swatchCount = request.swatchCount,
            )
        }
            .map { colorSchemeDto ->
                with(colorSchemeMapper) { colorSchemeDto.toDomain() }
            }
        return with(resultMapper) { kotlinResult.toDomainResult() }
    }

    private fun Color.toDtoString(): String {
        val hex = with(colorConverter) { toAbstract().toHex() }
        return with(colorMapper) { hex.toHexString() }
    }
}