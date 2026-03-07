package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Tracks the number of concurrent operations and invokes the [onCounterChange] callback
 * when the count changes.
 *
 * This component is thread-safe.
 * The [onCounterChange] callback is invoked immediately after each increment or decrement.
 */
@OptIn(ExperimentalContracts::class)
class OperationCounter(
    private val onCounterChange: OnCounterChangeAction,
) {
    @Volatile
    var updateCounter = 0
        private set
    private val mutex = Mutex()

    suspend fun withCounter(
        owner: Any? = null,
        block: suspend () -> Unit,
    ) {
        contract {
            // coroutine may get canceled before 'block' is invoked
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }
        mutex.withLock(owner) {
            updateCounter++
            onCounterChange(updateCounter, owner)
        }
        try {
            block()
        } finally {
            mutex.withLock(owner) {
                updateCounter--
                onCounterChange(updateCounter, owner)
            }
        }
    }

    fun interface OnCounterChangeAction {
        operator fun invoke(counter: Int, owner: Any?)
    }
}