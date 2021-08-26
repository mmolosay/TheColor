package com.ordolabs.data.repository

import com.ordolabs.data_remote.api.TheColorApiService
import com.ordolabs.data_remote.mapper.toDomain
import com.ordolabs.domain.model.ColorInformation
import com.ordolabs.domain.repository.IColorRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ColorRemoteRepository(
    private val api: TheColorApiService
) : IColorRemoteRepository {

    override suspend fun fetchColorInformation(colorHex: String): Flow<ColorInformation> = flow {
        val response = api.getColorInformation(hex = colorHex)
        emit(response.toDomain())
    }
}