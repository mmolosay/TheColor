package com.ordolabs.domain.repository

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.model.ColorScheme
import com.ordolabs.domain.model.ColorSchemeRequest
import kotlinx.coroutines.flow.Flow

interface IColorRemoteRepository {
    suspend fun fetchColorDetails(colorHex: String): Flow<ColorDetails>
    suspend fun fetchColorScheme(request: ColorSchemeRequest): Flow<ColorScheme>
}