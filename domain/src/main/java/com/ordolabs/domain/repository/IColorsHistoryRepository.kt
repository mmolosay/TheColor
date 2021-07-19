package com.ordolabs.domain.repository

import com.ordolabs.domain.model.ColorHistory
import kotlinx.coroutines.flow.Flow

interface IColorsHistoryRepository {

    fun getColorsFromHistory(): Flow<List<ColorHistory>>
}