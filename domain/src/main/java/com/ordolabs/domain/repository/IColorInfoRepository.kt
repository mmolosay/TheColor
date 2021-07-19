package com.ordolabs.domain.repository

import com.ordolabs.domain.model.Color
import kotlinx.coroutines.flow.Flow

interface IColorInfoRepository {
    suspend fun fetchColorInfo(): Flow<Color>
}