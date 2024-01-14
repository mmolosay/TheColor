package com.ordolabs.data.repository

import com.ordolabs.data_remote.api.TheColorApiService
import com.ordolabs.data_remote.mapper.toDomain
import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.model.ColorScheme
import com.ordolabs.domain.model.ColorSchemeRequest
import com.ordolabs.domain.repository.ColorRepository
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