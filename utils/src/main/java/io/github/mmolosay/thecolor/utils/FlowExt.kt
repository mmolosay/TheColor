package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.Flow
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

suspend fun <T> Flow<T>.firstWithTimeout(timeout: Duration): T {
    val flow = this
    return withTimeout(timeout) {
        flow.first()
    }
}

/**
 * A variation of [firstWithTimeout] with a short timeout.
 * This operator is designed to be used to get a value from a flow when the caller
 * expects it to be emitted promptly, e.g. due to the replay mechanism.
 */
suspend fun <T> Flow<T>.firstPronto(): T =
    this.firstWithTimeout(10.milliseconds)