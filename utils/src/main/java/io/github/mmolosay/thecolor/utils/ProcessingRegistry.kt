package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.ProcessingRegistry.Item
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A synchronized collection of values that are being processed and require tracking.
 * Commonly used to keep track of launched and ongoing coroutines.
 *
 * @param T the type of values (commands, events, operations, etc.).
 */
class ProcessingRegistry<T> {

    private val mutex = Mutex()
    private val _items = mutableListOf<Item<T>>()
    private val itemFactory = ItemFactory()

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
        val value: T,
        val id: Id,
        val job: Job?,
    ) {
        @JvmInline
        value class Id internal constructor(val value: Int)
    }

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

        fun add(value: T, job: Job?): Item.Id {
            checkSynchronization()
            val item = itemFactory.create(value, job)
            _items += item
            return item.id
        }

        fun remove(id: Item.Id): Item<T>? {
            checkSynchronization()
            val indexOfItem = _items.indexOfFirst { it.id == id }
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

private class ItemFactory {

    private var nextId = 0

    @Synchronized
    fun <T> create(value: T, job: Job?): Item<T> =
        Item(
            value = value,
            id = Item.Id(nextId++),
            job = job,
        )
}