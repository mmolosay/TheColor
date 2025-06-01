package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun <T> Flow<T?>.onEachNotNull(action: suspend (T) -> Unit): Flow<T?> =
    onEach { value ->
        value ?: return@onEach
        action(value)
    }

/**
 * It is a [DelicateCoroutinesApi].
 * The timeout may be a cause of malfunction when:
 * 1. debugging and waiting on breakpoints, allowing the timeout to expire.
 * 2. as underlying [withTimeout], it throws [TimeoutCancellationException].
 * This exception derives from a [CancellationException], which considered as non-fatal.
 * Thus, coroutine will be cancelled silently without propagating this exception to higher level.
 */
@DelicateCoroutinesApi
suspend fun <T> Flow<T>.firstWithTimeout(timeout: Duration): T {
    val flow = this
    return withTimeout(timeout) {
        flow.first()
    }
}

/**
 * A variation of [firstWithTimeout] with a short timeout.
 * This operator is designed to be used to get a value from a flow when the caller
 * expects it to be emitted promptly, e.g. due to the replay mechanism of `MutableFlow`.
 *
 * @see [firstWithTimeout]
 */
@DelicateCoroutinesApi
suspend fun <T> Flow<T>.firstPronto(): T =
    this.firstWithTimeout(10.milliseconds)

/**
 * Returns the first value emitted by the [StateFlow] after the current one (per se "next").
 * It is achieved by [drop]ping the replayed value of [StateFlow] and then taking the next one.
 */
suspend fun <T> StateFlow<T>.firstNext(): T =
    this
        .drop(1) // replayed value of StateFlow
        .first()