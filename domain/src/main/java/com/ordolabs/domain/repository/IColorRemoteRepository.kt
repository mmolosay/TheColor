package com.ordolabs.domain.repository

import com.ordolabs.domain.model.ColorDetails
import kotlinx.coroutines.flow.Flow

interface IColorRemoteRepository {
    suspend fun fetchColorDetails(colorHex: String): Flow<ColorDetails>
}