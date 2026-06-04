package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.ConsumableStore.Entry
import io.github.mmolosay.thecolor.utils.ConsumableStore.Id
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.transform
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private typealias ListOfEntries<T> = List<Entry<T>>

/**
 * A concurrent store of consumable values that remain available (stored) until explicitly consumed.
 *
 * Each published value is associated with a unique [Id] and appears in [flowOfPending] until it is
 * removed via [consume].
 *
 * This interface provides read and consume access only.
 * See [MutableConsumableStore] for publishing.
 *
 * @param T the type of values stored.
 */
/*
 * Related articles:
 * 1. https://github.com/Kotlin/kotlinx.coroutines/issues/2886
 * 2. https://proandroiddev.com/android-one-off-events-approaches-evolution-anti-patterns-add887cd0250
 * 3. https://itnext.io/exercises-in-futility-one-time-events-in-android-ddbdd7b5bd1c
 */
interface ConsumableStore<T> {

    /**
     * Contains values that were [publish][MutableConsumableStore.publish]ed but not yet [consume]d.
     *
     * The contained [List] is immutable.
     */
    val flowOfPending: StateFlow<List<Entry<T>>>

    /**
     * Removes the value with the specified [id] from the [flowOfPending] values.
     * The value is considered "consumed" from the client's perspective when it has been fully
     * processed by the said client and can be safely removed from the store.
     *
     * This method is safe for concurrency.
     * Access synchronization is fair and obeys FIFO rule.
     * Invoking this method more than once with the same [id] is idempotent.
     *
     * @return `true` if there was a value with the specified [id] in the [flowOfPending],
     * and it was removed; `false` otherwise.
     */
    fun consume(id: Id): Boolean

    @ConsistentCopyVisibility
    data class Entry<out T> internal constructor(
        val id: Id,
        val value: T,
    )

    @JvmInline
    value class Id internal constructor(val value: Int)
}

val <T> ConsumableStore<T>.pending: List<Entry<T>>
    get() = this.flowOfPending.value

fun <T> List<Entry<T>>.values(): List<T> =
    this.map { it.value }

fun <T> List<Entry<T>>.ids(): List<Id> =
    this.map { it.id }

fun <T> ConsumableStore<T>.consume(entry: Entry<T>): Boolean =
    this.consume(id = entry.id)

fun <T> ConsumableStore<T>.requireConsume(id: Id) {
    val success = consume(id)
    require(success) { "The value with ID=$id was not found in the pending values" }
}

/**
 * A type of [ConsumableStore] with [publish]ing capabilities.
 */
interface MutableConsumableStore<T> : ConsumableStore<T> {

    /**
     * Adds the specified [value] to the [flowOfPending] values.
     *
     * This method is safe for concurrency.
     * Access synchronization is fair and obeys FIFO rule.
     *
     * A new [Id] generated for the [value] is incremental and strictly sequential, without skipping.
     * This means that for the new value, the [Id.value] will always be the very next integer after the
     * last [Id] generated (for the last [publish]ed value).
     */
    fun publish(value: T)
}

fun <T> MutableConsumableStore(): MutableConsumableStore<T> =
    MutableConsumableStoreImpl<T>()

/**
 * Returns an immutable, read-only view of the receiver [MutableConsumableStore].
 */
fun <T> MutableConsumableStore<T>.asConsumableStore(): ConsumableStore<T> =
    ConsumableStoreImpl(delegate = this)

internal class MutableConsumableStoreImpl<T> : MutableConsumableStore<T> {

    private val mapOfPending = LinkedHashMap<Id, T>()
    private val _flowOfPending = MutableStateFlow<ListOfEntries<T>>(mapOfPending.toImmutableList())
    override val flowOfPending: StateFlow<ListOfEntries<T>> = _flowOfPending.asStateFlow()

    private val lock = ReentrantLock(/* fair = */ true)
    private val idFactory = IdFactory()

    override fun publish(value: T) {
        lock.withLock {
            val newId = idFactory.next()
            modifyPending { put(newId, value) }
        }
    }

    override fun consume(id: Id): Boolean =
        lock.withLock {
            val wasRemoved = modifyPending { remove(id) != null }
            return@withLock wasRemoved
        }

    @OptIn(ExperimentalContracts::class)
    private inline fun <R> modifyPending(block: MutableMap<Id, T>.() -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val initialMap = LinkedHashMap(mapOfPending)
        val result = with(mapOfPending) { block() }
        val hasMapBeenModified = (initialMap != mapOfPending)
        if (hasMapBeenModified) {
            _flowOfPending.value = mapOfPending.toImmutableList() // new reference for StateFlow update and immutable copy for clients
        }
        return result
    }

    private fun Map<Id, T>.toImmutableList(): ListOfEntries<T> =
        this.entries
            .map { (id, value) -> Entry(id = id, value = value) }
            .toPersistentList()

    private class IdFactory {
        private val nextIntToUse = AtomicInteger(0)
        fun next(): Id = Id(nextIntToUse.getAndIncrement())
    }
}

internal class ConsumableStoreImpl<T>(
    delegate: ConsumableStore<T>,
) : ConsumableStore<T> by delegate

/**
 * Transforms [flowOfPending][ConsumableStore.flowOfPending] values into a __cold__ [Flow] that
 * emits each pending [Entry] exactly once per collector.
 *
 * Upon collection, the flow emits all entries currently present in [flowOfPending][ConsumableStore.flowOfPending],
 * followed by any newly published entries. Entries are emitted in publication order and are
 * never re-emitted to the same collector.
 *
 * This function __does not__ consume entries.
 * Callers must invoke [consume][MutableConsumableStore.consume] explicitly.
 */
fun <T> ConsumableStore<T>.pendingAsFlow(): Flow<Entry<T>> {
    val seenIds = mutableSetOf<Id>()
    return this.flowOfPending.transform { listOfPending ->
        for (entry in listOfPending) {
            val isNew = seenIds.add(entry.id)
            if (isNew) emit(entry)
        }
    }
}

suspend inline fun <T> ConsumableStore<T>.consumePendingAsFlow(
    crossinline process: (Entry<T>) -> Unit,
) {
    val store = this
    val pendingAsFlow = store.pendingAsFlow()
    pendingAsFlow.collect { pendingEntry ->
        process(pendingEntry)
        store.consume(id = pendingEntry.id)
    }
}