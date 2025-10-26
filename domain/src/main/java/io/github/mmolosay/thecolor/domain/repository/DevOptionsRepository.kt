package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.DevOptions.PredictableRandomColors
import kotlinx.coroutines.flow.StateFlow

/**
 * Unlike [UserPreferencesRepository], all Flows are not-nullable.
 * If a particular flow emits value, that means that either
 * a: this exact value is stored, or
 * b: there is no value stored for this feature, and the returned value is a default one, or
 * c: the flow is only being initialized yet, and the returned value is a default one.
 */
interface DevOptionsRepository {
    val flowOfPredictableRandomColors: StateFlow<PredictableRandomColors>
    suspend fun setPredictableRandomColors(value: PredictableRandomColors?)
}