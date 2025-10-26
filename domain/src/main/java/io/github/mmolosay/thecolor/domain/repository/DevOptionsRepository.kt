package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.DevOptions.PredictableRandomColors
import kotlinx.coroutines.flow.StateFlow

interface DevOptionsRepository {
    fun flowOfPredictableRandomColors(): StateFlow<PredictableRandomColors>
    suspend fun setPredictableRandomColors(value: PredictableRandomColors?)
}