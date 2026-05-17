package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Represents the initialization state of asynchronously loaded data.
 */
sealed interface DataState<out T> {
    data object BeingInitialized : DataState<Nothing>
    data class Ready<T>(val result: Result<T>) : DataState<T>

    sealed interface Result<out T> {
        data object NoValue : Result<Nothing>
        data class HasValue<T>(val value: T) : Result<T>
        data object InvalidValue : Result<Nothing>
    }
}

@OptIn(ExperimentalContracts::class)
fun <T> DataState<T>.getOrElse(
    block: () -> T,
): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is DataState.BeingInitialized -> block()
        is DataState.Ready -> this.result.getOrElse(block)
    }
}

fun <T> Flow<DataState<T>>.filterReady(): Flow<DataState.Ready<T>> =
    transform { dataState ->
        when (dataState) {
            is DataState.BeingInitialized -> return@transform
            is DataState.Ready -> emit(dataState)
        }
    }

@OptIn(ExperimentalContracts::class)
inline fun <T> DataState.Result<T>.getOrElse(
    block: () -> T,
): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is DataState.Result.NoValue -> block()
        is DataState.Result.HasValue -> this.value
        is DataState.Result.InvalidValue -> block()
    }
}