package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

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