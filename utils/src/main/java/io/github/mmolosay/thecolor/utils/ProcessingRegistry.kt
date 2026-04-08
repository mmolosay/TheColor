package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.ProcessingRegistry.Item
import kotlinx.coroutines.CancellationException
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

// region Extensions for ProcessingRegistry.AccessProvider

fun <T> ProcessingRegistry<T>.AccessProvider.removeAndCancel(id: Item.Id): Item<T>? =
    remove(id = id)?.also { item ->
        item.job?.cancel()
    }

// endregion

// region Extensions for ProcessingRegistry

suspend fun <T> ProcessingRegistry<T>.items(): List<Item<T>> =
    this.access {
        items
    }

suspend fun <T> ProcessingRegistry<T>.add(value: T, job: Job?): Item.Id =
    this.access {
        add(value = value, job = job)
    }

suspend fun <T> ProcessingRegistry<T>.remove(id: Item.Id): Item<T>? =
    this.access {
        remove(id = id)
    }

@Suppress("unused")
suspend fun <T> ProcessingRegistry<T>.removeAndCancel(id: Item.Id): Item<T>? =
    this.access {
        removeAndCancel(id)
    }

@Suppress("unused")
suspend fun <T> ProcessingRegistry<T>.requireRemove(id: Item.Id): Item<T> {
    val removed = this.remove(id)
    require(removed != null) { "The value with ID=$id was not found in the items" }
    return removed
}

/**
 * Wraps the execution of the [block], which is supposed to process the [value],
 * within an [add]-[remove] lifecycle.
 *
 * If the exception occurs inside the [block] (including coroutine's [CancellationException]),
 * then the [value] will still be [remove]d from the registry correctly, but the function will re-throw the exception.
 *
 * If so happens that by the time the [block] has finished executing and the [value] has already
 * been removed by some other code, then no exception will be thrown and the function will return gracefully.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T, R> ProcessingRegistry<T>.withRegistry(
    value: T,
    job: Job?,
    block: () -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val id = this.add(value, job)
    try {
        return block()
    } finally {
        this.remove(id)
    }
}

suspend inline fun <T> ProcessingRegistry<T>.removeAndCancelAll(): List<Item<T>> =
    this.removeAndCancelAll { true }

/**
 * [Removes][ProcessingRegistry.remove] and [cancels][Job.cancel] all items matching the given [predicate].
 *
 * @return the list of removed items.
 */
suspend inline fun <T> ProcessingRegistry<T>.removeAndCancelAll(
    crossinline predicate: (Item<T>) -> Boolean,
): List<Item<T>> =
    this.access {
        val items = this.items.filter(predicate)
        for (item in items) {
            removeAndCancel(item.id)
        }
        return@access items
    }

// endregion