package io.github.mmolosay.thecolor.presentation.common.viewmodel

import io.github.mmolosay.thecolor.utils.Maybe
import io.github.mmolosay.thecolor.utils.requireValue
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * A handle to a single value [T] within a tree of aggregated state.
 *
 * A `Store` may be the root of a tree or a [focus]ed view onto part of a larger one; callers
 * cannot tell which they hold and should not depend on it. Every store in a tree shares one source
 * of truth, so a change made through any store is reflected by every other store whose value
 * overlaps it.
 *
 * ### Consistency
 * Every value the tree exposes is a fully-aggregated, consistent snapshot: a change anywhere in the
 * tree becomes visible as a single transition, with no observable state in which part of the tree
 * has updated and the rest has not.
 *
 * ### Mutual exclusion
 * Writes across the tree are totally ordered: at most one write runs at a time, and writes never
 * interleave. Reads are never blocked by a write and never observe a partially-applied one.
 *
 * Safe to use from concurrent coroutines on any dispatcher.
 *
 * @param T the type of value this store exposes.
 */
interface Store<T> {

    /**
     * The current committed value.
     *
     * Not transaction-aware: inside a [transaction] it reflects the committed state, not changes
     * made so far within that transaction. For a read that observes in-flight transaction changes,
     * or the read step of a read-modify-write, see [current].
     */
    val value: T

    /**
     * The store's value as a stream of its changes over time.
     *
     * Cold and conflating. Every emitted value is a fully-aggregated, committed snapshot.
     *
     * Eventually consistent with respect to writes: after an [update] or [transaction] completes, a
     * corresponding value follows after a brief delay. [current] is immediate and authoritative;
     * [value] is the synchronous committed read.
     */
    val flow: Flow<T>

    /**
     * The store's value, authoritative for the calling context.
     *
     * Outside a [transaction] this is the committed value. Inside one it reflects the changes made
     * so far within that same transaction — including uncommitted ones, not yet observable through
     * [value] or [flow].
     *
     * The only read never stale with respect to writes already applied in the calling context;
     * [value] can miss an uncommitted change made earlier in the same transaction.
     */
    suspend fun current(): T

    /**
     * Atomically replaces the value with the result of applying [transform] to it.
     *
     * [transform] must derive its result from the value it receives, not from one captured earlier,
     * so it composes correctly when writes interleave; it may run against a value newer than the
     * caller last observed.
     *
     * Outside a [transaction] the change commits on its own, as a single consistent transition.
     * Inside one it contributes to the transaction and commits with it. It never interleaves with
     * another write.
     *
     * @param transform maps the received value to the new one.
     */
    suspend fun update(transform: (T) -> T)

    /**
     * Runs [block] as a single atomic, isolated unit of work.
     *
     * While it runs, no other write begins or commits: values [block] reads through [current] are
     * stable, and every change it makes commits together as one transition, observable to others
     * only after [block] returns. If [block] throws or is canceled, nothing is published and the
     * tree is left as if it never ran.
     *
     * Concurrent transactions on the tree are serialized: one runs to completion and commits before
     * the next begins, each as its own transition. A transaction begun while one is already in
     * progress *on the same coroutine* instead joins it and commits with it, rather than deadlocking.
     *
     * [block] may suspend, including switching context via `withContext`; the guarantees hold across
     * suspension and context switches. Only writes run inline within [block] participate: a write
     * spawned in a separate coroutine (via `launch`/`async`) does not join the transaction and commits
     * on its own, and awaiting one from within [block] deadlocks against the transaction's exclusion.
     *
     * @param block the work to run.
     * @return the result of [block].
     */
    suspend fun <R> transaction(block: suspend () -> R): R

    /**
     * A [Store] view onto the part of this store's value selected by [lens].
     *
     * Another view of the same state: reads project through [lens]; writes apply back through
     * [lens] into this store and the whole tree. It shares this store's consistency and
     * mutual-exclusion guarantees and participates in its [transaction]s.
     *
     * @param lens selects the sub-value and defines how to write it back.
     * @param V the type of the focused sub-value.
     */
    fun <V> focus(lens: Lens<T, V>): Store<V>
}

fun <T> Store(initial: T): Store<T> =
    RootStore(initial)

private class RootStore<T>(initial: T) : Store<T> {

    override val value: T
        get() = _flow.value

    private val _flow = MutableStateFlow(initial)
    override val flow: StateFlow<T> = _flow.asStateFlow()

    private val writeMutex = Mutex()
    private var working: Maybe<T> = Maybe.None
    private val txId = Any()

    override suspend fun current(): T =
        if (inTransaction()) {
            working.requireValue()
        } else {
            value
        }

    override suspend fun update(transform: (T) -> T) =
        if (inTransaction()) {
            val value = working.requireValue()
            val newValue = transform(value)
            working = Maybe.Some(newValue)
        } else {
            writeMutex.withLock {
                _flow.update(transform)
            }
        }

    override suspend fun <R> transaction(block: suspend () -> R): R =
        if (inTransaction()) {
            block()
        } else {
            writeMutex.withLock {
                working = Maybe.Some(value)
                try {
                    val newTxMarker = TxMarker(txId)
                    val result = withContext(newTxMarker) {
                        block()
                    }
                    _flow.value = working.requireValue()
                    return@withLock result
                } finally {
                    working = Maybe.None
                }
            }
        }

    override fun <V> focus(lens: Lens<T, V>): Store<V> =
        StoreView(
            store = this,
            lens = lens,
        )

    private class TxMarker(val id: Any) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<TxMarker>
    }

    private suspend fun inTransaction(): Boolean {
        val txMarker = currentCoroutineContext()[TxMarker.Key] ?: return false
        return (txMarker.id === txId)
    }
}

private class StoreView<Source, T>(
    private val store: Store<Source>,
    private val lens: Lens<Source, T>,
) : Store<T> {

    override val value: T
        get() = lens.get(store.value)

    override val flow: Flow<T> =
        store.flow
            .map { lens.get(it) }
            .distinctUntilChanged()

    override suspend fun current(): T =
        lens.get(source = store.current())

    override suspend fun update(transform: (T) -> T) =
        store.update { current ->
            val value = lens.get(current)
            val newValue = transform(value)
            lens.set(current, newValue)
        }

    override suspend fun <R> transaction(block: suspend () -> R): R =
        store.transaction(block)

    override fun <V> focus(lens: Lens<T, V>): Store<V> =
        StoreView(
            store = this,
            lens = lens,
        )
}

interface Lens<Source, Value> {
    fun get(source: Source): Value
    fun set(source: Source, value: Value): Source
}

fun <S, V> Lens(
    get: (source: S) -> V,
    set: (source: S, value: V) -> S,
): Lens<S, V> =
    object : Lens<S, V> {
        override fun get(source: S): V = get(source)
        override fun set(source: S, value: V): S = set(source, value)
    }

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This write does not impose its own call-ordering. " +
            "The caller must serialize it — e.g. by confining calls to a single-threaded context, or by issuing them sequentially within a single transaction. " +
            "Calling it from a context that does not guarantee ordering can let concurrent writes apply out of order (last-write-wins races). " +
            "If your context does not provide ordering, use the safe, ordered API instead.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class RequiresWriteOrdering