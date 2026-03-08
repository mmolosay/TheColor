package io.github.mmolosay.thecolor.data.remote

import io.github.mmolosay.thecolor.data.remote.api.TheColorApiService
import io.github.mmolosay.thecolor.data.remote.mapper.ColorDetailsMapper
import io.github.mmolosay.thecolor.data.remote.mapper.ColorMapper
import io.github.mmolosay.thecolor.data.remote.mapper.ColorSchemeMapper
import io.github.mmolosay.thecolor.data.remote.mapper.toDto
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorDetails
import io.github.mmolosay.thecolor.domain.color.ColorRepository
import io.github.mmolosay.thecolor.domain.color.ColorRepository.GetColorSchemeRequest
import io.github.mmolosay.thecolor.domain.color.ColorScheme
import io.github.mmolosay.thecolor.domain.exception.DomainFailureFactory
import io.github.mmolosay.thecolor.domain.exception.tryMapFailureToDomain
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
    private val domainFailureFactory: DomainFailureFactory,
) : ColorRepository {

    override suspend fun getColorDetails(color: Color): Result<ColorDetails> {
        val colorString = color.toDtoString()
        return runCatching {
            api.getColorDetails(hex = colorString)
        }
            .map { colorDetailsDto ->
                with(colorDetailsMapper) { colorDetailsDto.toDomain() }
            }
            .tryMapFailureToDomain(domainFailureFactory)
    }

    override suspend fun getColorScheme(request: GetColorSchemeRequest): Result<ColorScheme> {
        val seedHex = request.seed.toDtoString()
        return runCatching {
            api.getColorScheme(
                hex = seedHex,
                mode = request.mode.toDto(),
                swatchCount = request.swatchCount,
            )
        }
            .map { colorSchemeDto ->
                with(colorSchemeMapper) { colorSchemeDto.toDomain() }
            }
            .tryMapFailureToDomain(domainFailureFactory)
    }

    private fun Color.toDtoString(): String {
        val hex = with(colorConverter) { toHex() }
        return with(colorMapper) { hex.toHexString() }
    }
}