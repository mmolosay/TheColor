package io.github.mmolosay.thecolor.domain.dev.options

import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.HttpLogging
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.PredictableRandomColors
import io.github.mmolosay.thecolor.domain.dev.options.DevOptions.StrictMode
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository.DataState
import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository.IllegalStoredValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface DevOptionsRepository {
    val flowOfPredictableRandomColors: StateFlow<DataState<PredictableRandomColors>>
    suspend fun setPredictableRandomColors(value: PredictableRandomColors?)

    val flowOfStrictMode: StateFlow<DataState<StrictMode>>
    suspend fun setStrictMode(value: StrictMode?)

    val flowOfHttpLogging: StateFlow<DataState<HttpLogging>>
    suspend fun setHttpLogging(value: HttpLogging?)

    sealed interface DataState<out T> {
        data object BeingInitialized : DataState<Nothing>
        data object NoValueStored : DataState<Nothing>
        data class HasValueStored<T>(val value: T) : DataState<T>
    }

    class IllegalStoredValue(propertyName: String, value: Any?) : IllegalStateException(
        "Property '$propertyName' has unsupported stored value: $value"
    )
}

@OptIn(ExperimentalContracts::class)
inline fun <T> DataState<T>.valueOrElse(
    block: () -> T,
): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is DataState.BeingInitialized -> block()
        is DataState.NoValueStored -> block()
        is DataState.HasValueStored -> this.value
    }
}

fun <T> Flow<DataState<T>>.filterOutBeingInitialized(): Flow<DataState<T>> =
    this.filter { it !is DataState.BeingInitialized }

// syntactic sugar
inline fun <reified T> IllegalStoredValue(value: Any?) =
    IllegalStoredValue(
        propertyName = T::class.simpleName!!,
        value = value,
    )