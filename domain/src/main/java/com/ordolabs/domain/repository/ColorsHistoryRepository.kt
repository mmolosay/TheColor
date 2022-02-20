package com.ordolabs.domain.repository

import com.ordolabs.domain.model.HistoryColor
import kotlinx.coroutines.flow.Flow

interface ColorsHistoryRepository {

    fun getColorsFromHistory(): Flow<List<HistoryColor>>
}