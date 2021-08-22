package com.ordolabs.domain.repository

import com.ordolabs.domain.model.ColorInformation
import kotlinx.coroutines.flow.Flow

interface IColorRemoteRepository {
    suspend fun fetchColorInformation(colorHex: String): Flow<ColorInformation>
}