package io.github.mmolosay.thecolor.domain.dev.options

import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.HttpLogging
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.StrictMode
import io.github.mmolosay.thecolor.utils.DataState
import kotlinx.coroutines.flow.StateFlow

interface DevOptionsRepository {
    val flowOfPredictableRandomColors: StateFlow<DataState<PredictableRandomColors>>
    suspend fun setPredictableRandomColors(value: PredictableRandomColors?)

    val flowOfStrictMode: StateFlow<DataState<StrictMode>>
    suspend fun setStrictMode(value: StrictMode?)

    val flowOfHttpLogging: StateFlow<DataState<HttpLogging>>
    suspend fun setHttpLogging(value: HttpLogging?)

    suspend fun clear()
}