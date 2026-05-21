package io.github.mmolosay.thecolor.domain.dev.options

import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.HttpLogging
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.StrictMode
import io.github.mmolosay.thecolor.domain.utils.PrefState
import kotlinx.coroutines.flow.StateFlow

interface DevOptionsRepository {
    val flowOfPredictableRandomColors: StateFlow<PrefState<PredictableRandomColors>>
    suspend fun setPredictableRandomColors(value: PredictableRandomColors?)

    val flowOfStrictMode: StateFlow<PrefState<StrictMode>>
    suspend fun setStrictMode(value: StrictMode?)

    val flowOfHttpLogging: StateFlow<PrefState<HttpLogging>>
    suspend fun setHttpLogging(value: HttpLogging?)

    suspend fun clear()
}