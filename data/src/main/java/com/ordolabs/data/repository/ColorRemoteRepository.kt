package com.ordolabs.data.repository

import com.ordolabs.data_remote.api.TheColorApiService
import com.ordolabs.data_remote.mapper.toDomain
import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.repository.IColorRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ColorRemoteRepository(
    private val api: TheColorApiService
) : IColorRemoteRepository {

    override suspend fun fetchColorDetails(colorHex: String): Flow<ColorDetails> = flow {
        val response = api.getColorDetails(hex = colorHex)
        emit(response.toDomain())
    }
}