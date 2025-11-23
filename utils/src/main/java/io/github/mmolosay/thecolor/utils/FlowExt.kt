package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

fun <T> Flow<T?>.onEachNotNull(action: suspend (T) -> Unit): Flow<T?> =
    onEach { value ->
        value ?: return@onEach
        action(value)
    }

/**
 * Returns the first value emitted by the [StateFlow] after the current one (per se "next").
 * It is achieved by [drop]ping the replayed value of [StateFlow] and then taking the next one.
 */
suspend fun <T> StateFlow<T>.firstNext(): T =
    this
        .drop(1) // replayed value of StateFlow
        .first()

/**
 * Returns a flow with [map] and [distinctUntilChanged] operators applied to it.
 * This operator is a shortcut for mapping a [StateFlow].

 * Although the receiver is a [StateFlow], if its value changes but [transform] produces
 * the same value, then two same values will be emitted from the returned flow in succession.
 */
inline fun <T, R> StateFlow<T>.mapDistinctly(
    crossinline transform: suspend (value: T) -> R,
): Flow<R> =
    this
        .map(transform)
        .distinctUntilChanged()

/**
 * Performs [tryEmit()][MutableSharedFlow.tryEmit] with assertion that the [value]
 * was indeed emitted.
 * See [MutableSharedFlow.tryEmit] for more details.
 *
 * For [Channel][kotlinx.coroutines.channels.Channel] variation see [SendChannel.requireSend()][kotlinx.coroutines.channels.SendChannel.requireSend].
 */
fun <T> MutableSharedFlow<T>.requireEmit(value: T) {
    val wasEmitted = this.tryEmit(value)
    require(wasEmitted) { "Failed to emit value $value" }
}

/**
 * This operator combines the source flow with an update flag flow, and suppresses
 * any emissions while the update flag is `true` (data is actively being updated).
 * Once the flag becomes `false` (update has finished), it emits the most recent value from the source flow.
 *
 * This is useful to prevent intermediate, unstable values from being collected while some
 * external update operation is in progress.
 *
 * You may want to apply [distinctUntilChanged] after this operator.
 * If flow of [isBeingUpdated] updates without source flow emitting a new value,
 * then the latest value will be re-emitted when [isBeingUpdated] becomes `false`.
 *
 * @param isBeingUpdated A flow indicating whether data is currently being updated.
 * One can think of it as of "is the source flow actively emitting non-final values" flow.
 */
fun <T> Flow<T>.stabilize(
    isBeingUpdated: Flow<Boolean>,
): Flow<T> =
    combineTransform(this, isBeingUpdated) { value, isBeingUpdated ->
        if (!isBeingUpdated) emit(value)
    }