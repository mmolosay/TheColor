package com.ordolabs.domain.repository

import com.ordolabs.domain.model.ColorDetails
import com.ordolabs.domain.model.ColorScheme
import com.ordolabs.domain.model.ColorSchemeRequest
import kotlinx.coroutines.flow.Flow

interface ColorRepository {
    suspend fun getColorDetails(colorHex: String): Flow<ColorDetails>
    suspend fun getColorScheme(request: ColorSchemeRequest): Flow<ColorScheme>
}