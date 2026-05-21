package io.github.mmolosay.thecolor.domain.utils

import io.github.mmolosay.thecolor.domain.dev.options.DevOptionsRepository
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Represents the state of a 'preference' in the Domain layer.
 *
 * 'Preference' is a persisted value that can be absent, invalid, or successfully fetched.
 * It is represented by [Result] monad.
 *
 * This class should be used in Domain repositories, such as
 * [UserPreferencesRepository] and [DevOptionsRepository].
 */
sealed interface PrefState<out T> {

    data object BeingInitialized : PrefState<Nothing>
    data class Ready<T>(val result: Result<T>) : PrefState<T>

    sealed interface Result<out T> {
        data object NoValue : Result<Nothing>
        data object InvalidValue : Result<Nothing>
        data class HasValue<T>(val value: T) : Result<T>
    }
}

@OptIn(ExperimentalContracts::class)
fun <T> PrefState<T>.getOrElse(
    block: () -> T,
): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is PrefState.BeingInitialized -> block()
        is PrefState.Ready -> this.result.getOrElse(block)
    }
}

fun <T> Flow<PrefState<T>>.filterReady(): Flow<PrefState.Ready<T>> =
    transform { dataState ->
        when (dataState) {
            is PrefState.BeingInitialized -> return@transform
            is PrefState.Ready -> emit(dataState)
        }
    }

@OptIn(ExperimentalContracts::class)
inline fun <T> PrefState.Result<T>.getOrElse(
    block: () -> T,
): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is PrefState.Result.NoValue -> block()
        is PrefState.Result.HasValue -> this.value
        is PrefState.Result.InvalidValue -> block()
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T, R> PrefState.Result<T>.map(
    transform: (T) -> R,
): PrefState.Result<R> {
    contract {
        callsInPlace(transform, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is PrefState.Result.NoValue -> this
        is PrefState.Result.HasValue -> PrefState.Result.HasValue(value = transform(this.value))
        is PrefState.Result.InvalidValue -> this
    }
}