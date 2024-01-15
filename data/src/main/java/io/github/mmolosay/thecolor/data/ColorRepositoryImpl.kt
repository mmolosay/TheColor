package io.github.mmolosay.thecolor.data

import io.github.mmolosay.thecolor.data.remote.api.TheColorApiService
import io.github.mmolosay.thecolor.data.remote.mapper.toDomain
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorSchemeRequest
import io.github.mmolosay.thecolor.domain.repository.ColorRepository
import javax.inject.Inject

class ColorRepositoryImpl @Inject constructor(
    private val api: TheColorApiService
) : ColorRepository {

    override suspend fun getColorDetails(colorHex: String): ColorDetails {
        val response = api.getColorDetails(hex = colorHex)
        return response.toDomain()
    }

    override suspend fun getColorScheme(request: ColorSchemeRequest): ColorScheme {
        val mode = TheColorApiService.SchemeMode.values()[request.modeOrdinal]
        val response = api.getColorScheme(
            hex = request.seedHex,
            mode = mode,
            sampleCount = request.sampleCount,
        )
        return response.toDomain()
    }
}