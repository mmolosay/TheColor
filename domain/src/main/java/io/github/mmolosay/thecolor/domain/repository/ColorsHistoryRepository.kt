package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.HistoryColor
import kotlinx.coroutines.flow.Flow

interface ColorsHistoryRepository {

    fun getColorsFromHistory(): Flow<List<HistoryColor>>
}