package io.github.mmolosay.thecolor.presentation.common.viewmodel

import io.github.mmolosay.thecolor.utils.Maybe
import io.github.mmolosay.thecolor.utils.requireValue
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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
 * Every value ever exposed by [flow], read from [value], or returned by [current] is a
 * fully-aggregated, internally consistent snapshot of the tree. A change originating anywhere in
 * the tree becomes visible as a single transition; there is no observable intermediate state in
 * which part of the tree has updated and the rest has not.
 *
 * ### Mutual exclusion
 * Writes across the entire tree are totally ordered: at most one [update] or [transaction] is in
 * progress at a time, and a write never interleaves with another. Reads ([value], [flow],
 * [current]) are never blocked by an in-progress write and never observe a partially-applied one.
 *
 * ### Observation
 * [flow] is a *cold* stream: it carries no value of its own and does not conflate. Collect it
 * through [stateIn][kotlinx.coroutines.flow.stateIn] (or another conflating, hot terminal) to
 * obtain a de-duplicated [StateFlow][kotlinx.coroutines.flow.StateFlow] bound to a scope; that
 * `StateFlow` collapses consecutive equal values. For a synchronous read of the current value
 * without collecting, use [value].
 *
 * Implementations are safe to use from multiple coroutines on any dispatcher.
 *
 * @param T the type of value this store exposes.
 */
interface Store<T> { // TODO: refine Store's KDoc above and show the file to Claude to check for any issues

    /**
     * The current committed value, read synchronously.
     *
     * This is the latest committed value and is **not** transaction-aware: inside a [transaction]
     * it still reflects the committed state, not the changes made so far within that transaction.
     * For the read step of a read-modify-write, or any read that must observe in-flight
     * transaction changes, use [current] instead.
     *
     * Suitable as the initial value when collecting [flow] via
     * [stateIn][kotlinx.coroutines.flow.stateIn], and for fire-and-forget reads that tolerate the
     * eventually-consistent, non-transactional view.
     */
    val value: T // TODO: rename to "snapshot" to indicate that it is only commited values?

    /**
     * The store's value as an observable stream.
     *
     * Cold and non-conflating: collecting it restarts observation from the current value, and it may
     * emit consecutive equal values. Each value it emits is a fully-aggregated, consistent snapshot;
     * collectors never observe a partially-applied change.
     *
     * Eventually consistent with respect to writes: after an [update] or [transaction] completes, a
     * corresponding value follows after a brief propagation delay (because [Flow] is an asynchronous stream).
     * For an authoritative, immediate read — in particular the read step of a read-modify-write — use [current];
     * for a synchronous read of the current value, use [value].
     */
    val flow: Flow<T>

    /**
     * Returns this store's value authoritatively, awaiting any consistency the calling context
     * requires.
     *
     * Outside a [transaction] this is the committed value. Inside a [transaction] it reflects the
     * changes already made within that same transaction — including ones not yet committed, and so
     * not yet observable through [value] or [flow].
     *
     * This is the only read that is never stale with respect to writes already applied in the calling
     * context. Use it for the read step of a read-modify-write, where reading a committed-only value
     * (via [value]) could miss an uncommitted change made earlier in the same transaction.
     */
    suspend fun current(): T

    /**
     * Atomically replaces this store's value with the result of applying [transform] to it.
     *
     * [transform] must derive the new value from the value it receives, not from one captured earlier,
     * so that it composes correctly when other writes interleave; it may be invoked against a value
     * newer than the caller last observed.
     *
     * Outside a [transaction] the change is committed on its own and becomes visible as a single
     * consistent transition. Inside a [transaction] it instead contributes to that transaction and
     * becomes visible only when the transaction commits. Either way it does not interleave with any
     * other write to the tree.
     *
     * @param transform maps the current value to the new one.
     */
    suspend fun update(transform: (T) -> T)

    /**
     * Runs [block] as a single atomic, isolated unit of work over the tree.
     *
     * While it runs, no other write to the tree begins or commits: every value [block] reads through
     * [current] is stable, and every change it makes commits together as one consistent transition
     * that outside readers observe only once [block] returns. They never see an intermediate state.
     *
     * If [block] throws or is canceled, the transaction is abandoned: no change is published and the
     * tree is left as if it never ran.
     *
     * A transaction started while another is already in progress joins it and commits with it, rather
     * than starting a separate one. [block] may suspend; all of these guarantees hold across its
     * suspension points.
     *
     * Writes that participate in the transaction must run inline within [block].
     * A write spawned in a separate coroutine (e.g. via `launch`/`async`) does not join the transaction
     * and commits on its own, and awaiting such a write from within [block] deadlocks against the
     * transaction's own exclusion.
     * Make every change you want included a direct, inline [update] call.
     *
     * @param block the work to run; whatever it returns is returned by this call.
     * @return the result of [block].
     */
    suspend fun <R> transaction(block: suspend () -> R): R

    /**
     * Returns a [Store] onto the part of this store's value selected by [lens].
     *
     * The returned store is another view of the same underlying state: reading it projects this
     * store's value through [lens], and writing it applies the change back through [lens] into this
     * store and thus the whole tree. It shares this store's consistency and mutual-exclusion
     * guarantees and participates in the same [transaction]s.
     *
     * @param lens selects the sub-value to expose and defines how to write it back.
     * @param V the type of the focused sub-value.
     */
    fun <V> focus(lens: Lens<T, V>): Store<V>
}

fun <T> Store(initial: T): Store<T> =
    RootStore(initial)

/**
 * A single source of truth for one composition tree.
 */
private class RootStore<T>(initial: T) : Store<T> {

    override var value: T
        get() = _flow.value
        set(value) {
            _flow.value = value
        }

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
                    value = working.requireValue()
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