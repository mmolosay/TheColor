package com.ordolabs.domain.repository

import com.ordolabs.domain.model.ColorHex
import kotlinx.coroutines.flow.Flow

interface IColorInfoRepository {
    suspend fun fetchColorInfo(color: String): Flow<ColorHex>
}