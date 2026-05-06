package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A synchronized collection of a family of coroutines, each represented by a [Job]
 * and an associated value of type [T].
 *
 * @param T the type of values associated with coroutines (commands, events, payloads, etc.).
 */
class CoroutineRegistry<T> {

    private val mutex = Mutex()
    private val _items = mutableListOf<Item<T>>()

    /**
     * Executes the given [block] under the registry's internal [Mutex],
     * scoped to the [AccessProvider] for safe state updates and reads.
     */
    @OptIn(ExperimentalContracts::class)
    suspend fun <R> access(block: suspend AccessProvider.() -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val owner = Any()
        return mutex.withLock(owner) {
            val accessProvider = AccessProvider(owner)
            with(accessProvider) { block() }
        }
    }

    @ConsistentCopyVisibility
    data class Item<T> internal constructor(
        val job: Job,
        val value: T,
    )

    /**
     * Allows mutating and reading registry's items in a synchronized context
     * under the lock of the internal [mutex].
     */
    inner class AccessProvider(
        private val lockOwner: Any,
    ) {
        val items: List<Item<T>>
            get() {
                checkSynchronization()
                return _items.toList() // new instance for a truly immutable copy
            }

        fun add(job: Job, value: T): Item<T> {
            checkSynchronization()
            val item = Item(job = job, value = value)
            _items += item
            return item
        }

        fun remove(job: Job): Item<T>? {
            checkSynchronization()
            val indexOfItem = _items.indexOfFirst { it.job === job }
            if (indexOfItem != -1) {
                val item = _items.removeAt(indexOfItem)
                return item
            }
            return null
        }

        private fun checkSynchronization() {
            check(mutex.isLocked) { "Must be called under the mutex's lock" }
            check(mutex.holdsLock(lockOwner)) { "This AccessProvider doesn't belong to the current mutex's lock" }
        }
    }
}