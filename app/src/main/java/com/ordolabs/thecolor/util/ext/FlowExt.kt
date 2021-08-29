package com.ordolabs.thecolor.util.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * Emits values that are originally being collected in [Flow] in [destination],
 * applying specified [action] [onEach] source `Flow` value.
 */
suspend inline fun <S, D> Flow<S>.emitIn(
    destination: MutableSharedFlow<D>,
    crossinline action: (S) -> D?
) =
    this.collect { value ->
        val mapped = action(value) ?: return@collect
        destination.emit(mapped)
    }
