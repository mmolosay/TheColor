package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.Job
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A synchronized collection of a family of coroutines, each represented by a [Job] and an
 * associated value of type [T] that identifies it.
 *
 * Provides storage only: adding, removing and reading items. Everything built on top of it —
 * superseding, canceling, launching — is defined as extension functions.
 *
 * ### Mutual exclusion
 * Every read and mutation is performed under the internal lock. Multi-step operations that must be
 * atomic are performed in a single [access] block.
 *
 * The lock is reentrant, so a nested [access] on the same thread joins the outer one instead of
 * deadlocking. While the nested block runs, the outer [AccessProvider] is not usable: the inner one
 * is the only valid handle, and the outer one becomes valid again once the nested block returns.
 *
 * Canceling and joining coroutines must always be performed *outside* of [access]. A canceled
 * coroutine may complete synchronously on the calling thread, and its completion handler mutates
 * this collection; because the lock is reentrant, doing that under the lock would let the mutation
 * through in the middle of a critical section instead of blocking it.
 *
 * Safe to use from concurrent coroutines on any dispatcher.
 *
 * @param T the type of values associated with coroutines (commands, events, payloads, etc.).
 */
class CoroutineRegistry<T> {

    private val lock = ReentrantLock()
    private val _items = mutableListOf<Item<T>>()
    private var accessOwner: Any? = null

    /**
     * Executes the given [block] under the registry's internal lock, scoped to the [AccessProvider]
     * for safe reads and mutations.
     *
     * Everything the [block] observes and changes is atomic with respect to other accesses.
     * The [block] must not cancel or join coroutines, see the documentation of this class.
     */
    fun <R> access(block: AccessProvider.() -> R): R =
        lock.withLock {
            val owner = Any()
            val previousOwner = accessOwner
            accessOwner = owner
            try {
                val accessProvider = AccessProvider(owner)
                with(accessProvider) { block() }
            } finally {
                accessOwner = previousOwner
            }
        }

    /**
     * Allows mutating and reading registry's items in a synchronized context under the internal lock.
     */
    inner class AccessProvider internal constructor(
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
            if (indexOfItem == -1) return null
            return _items.removeAt(indexOfItem)
        }

        private fun checkSynchronization() {
            check(lock.isHeldByCurrentThread) { "Must be called under the lock" }
            check(accessOwner === lockOwner) { "This AccessProvider doesn't belong to the current access block" }
        }
    }

    @ConsistentCopyVisibility
    data class Item<T> internal constructor(
        val job: Job,
        val value: T,
    )
}