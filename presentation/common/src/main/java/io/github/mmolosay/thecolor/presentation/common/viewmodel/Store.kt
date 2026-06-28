package io.github.mmolosay.thecolor.presentation.common.viewmodel

import io.github.mmolosay.thecolor.utils.Maybe
import io.github.mmolosay.thecolor.utils.requireValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * A reactive, observable handle to a single value [T] within a tree of composable state.
 *
 * A `Store` may be the root of a tree or a [focus]ed view onto part of a larger one; callers
 * cannot distinguish the two and should not depend on which they hold. Every store in a tree
 * shares one underlying source of truth, so a change made through any store is reflected by
 * every other store whose value overlaps it.
 *
 * ### Consistency
 * Every value ever exposed by [flow] or returned by [current] is a fully-aggregated, internally
 * consistent snapshot of the tree. A change originating anywhere in the tree becomes visible as a
 * single transition; there is no observable intermediate state in which part of the tree has
 * updated and the rest has not.
 *
 * ### Mutual exclusion
 * Writes across the entire tree are totally ordered: at most one [update] or [transaction] is in
 * progress at a time, and a write never interleaves with another. Reads ([flow], [current]) are
 * never blocked by an in-progress write and never observe a partially-applied one.
 *
 * Implementations are safe to use from multiple coroutines on any dispatcher.
 *
 * @param T the type of value this store exposes.
 */
interface Store<T> {

    /**
     * The current value as an observable stream.
     *
     * Always holds a fully-aggregated, consistent snapshot. Emits a new value only when this
     * store's value actually changes; a change elsewhere in the tree that does not affect this
     * store's value produces no emission. Collectors observe a glitch-free sequence and never see
     * a partially-applied change.
     *
     * This stream is eventually consistent with respect to writes: immediately after an [update]
     * or [transaction] completes, it reflects the new value after a brief propagation delay. For an
     * authoritative, immediate read — in particular the read step of a read-modify-write — use
     * [current] instead.
     */
    val flow: StateFlow<T>

    /**
     * Returns this store's value authoritatively and without delay.
     *
     * Outside a [transaction] this is the committed value.
     * Inside a [transaction] it reflects the changes made so far within that same transaction,
     * including ones not yet visible to [flow] or to readers outside the transaction.
     *
     * Prefer this over [flow]'s value for the read step of a read-modify-write, since it is never
     * stale with respect to writes already applied in the current context.
     */
    suspend fun current(): T

    /**
     * Atomically replaces this store's value with the result of applying [transform] function to it.
     *
     * [transform] receives the current value and must return the new one; express changes as a
     * function of the received value (rather than a value captured earlier) so the update composes
     * correctly with concurrent writes. The change is published as a single consistent transition.
     *
     * When called outside a [transaction], the update is committed on its own and becomes visible
     * immediately. When called inside a [transaction], it instead contributes to that transaction
     * and becomes visible only when the transaction completes.
     *
     * Runs under the tree's mutual exclusion: it does not interleave with any other write.
     *
     * @param transform a pure function mapping a current value to a new one.
     */
    suspend fun update(transform: (T) -> T)

    /**
     * Runs [block] as a single atomic, isolated unit of work over the tree, then commits.
     *
     * For the duration of the transaction no other write to the tree may begin or commit, so values
     * read via [current] are stable and every change made by [block] — through this store or any
     * other store in the tree — is applied together as one consistent transition when [block]
     * returns. Callers outside the transaction observe none of its intermediate changes, only the
     * final result.
     *
     * Use this to make several changes that must take effect together, or a read-modify-write whose
     * read must not be invalidated by another writer before the write lands. [block] may suspend;
     * the isolation holds across suspension points.
     *
     * If [block] throws or is canceled, the transaction is abandoned and no change is published;
     * the tree is left as if the transaction never ran.
     *
     * Transactions may be nested: a transaction started while one is already in progress joins the
     * outer one and commits with it.
     *
     * @param block the body of the transaction.
     * @return the result of [block] execution.
     */
    suspend fun <R> transaction(block: suspend () -> R): R

    /**
     * Returns a [Store] onto the part of this store's value selected by [lens].
     *
     * The returned store reads and writes the same underlying state through [lens]: reading it
     * projects this store's value, and updating it writes the change back into this store (and
     * thus the whole tree). It participates in the same consistency and mutual-exclusion guarantees
     * and the same [transaction]s as this store.
     *
     * Use this to give a component a handle to only its own view of a larger state, without
     * exposing the rest of the tree.
     *
     * @param lens selects the sub-value to expose and defines how to write it back.
     * @param scope the [CoroutineScope] that bounds the returned store's [flow];
     * when it is canceled the projection stops.
     * @param V the type of the focused sub-value.
     */
    fun <V> focus(lens: Lens<T, V>, scope: CoroutineScope): Store<V>
}

fun <T> Store(initial: T): Store<T> =
    RootStore(initial)

/**
 * A single source of truth for one composition tree.
 */
private class RootStore<T>(initial: T) : Store<T> {

    private val _flow = MutableStateFlow(initial)
    override val flow: StateFlow<T> = _flow.asStateFlow()

    private val writeMutex = Mutex()
    private var working: Maybe<T> = Maybe.None
    private val txId = Any()

    override suspend fun current(): T =
        if (inTransaction()) {
            working.requireValue()
        } else {
            _flow.value
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
                working = Maybe.Some(_flow.value)
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

    override fun <V> focus(
        lens: Lens<T, V>,
        scope: CoroutineScope,
    ): Store<V> =
        StoreView(
            store = this,
            lens = lens,
            scope = scope,
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
    scope: CoroutineScope,
) : Store<T> {

    override val flow: StateFlow<T> =
        store.flow
            .map { lens.get(it) }
            .stateIn(scope, SharingStarted.Lazily, lens.get(store.flow.value))

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

    override fun <V> focus(
        lens: Lens<T, V>,
        scope: CoroutineScope,
    ): Store<V> =
        StoreView(
            store = this,
            lens = lens,
            scope = scope,
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

infix fun <A, B, C> Lens<A, B>.then(other: Lens<B, C>): Lens<A, C> =
    CompoundLens<A, B, C>(outer = this, inner = other)

private class CompoundLens<A, B, C>(
    private val outer: Lens<A, B>,
    private val inner: Lens<B, C>,
) : Lens<A, C> {

    override fun get(source: A): C =
        inner.get(outer.get(source))

    override fun set(source: A, value: C): A =
        outer.set(source, inner.set(outer.get(source), value))
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