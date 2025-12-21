package io.github.mmolosay.thecolor.domain.repository

import io.github.mmolosay.thecolor.domain.model.DevOptions.PredictableRandomColors
import io.github.mmolosay.thecolor.domain.repository.DevOptionsRepository.DataState
import io.github.mmolosay.thecolor.domain.repository.DevOptionsRepository.IllegalStoredValue
import kotlinx.coroutines.flow.StateFlow
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface DevOptionsRepository {
    val flowOfPredictableRandomColors: StateFlow<DataState<PredictableRandomColors>>
    suspend fun setPredictableRandomColors(value: PredictableRandomColors?)

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

// syntactic sugar
inline fun <reified T> IllegalStoredValue(value: Any?) =
    IllegalStoredValue(
        propertyName = T::class.simpleName!!,
        value = value,
    )