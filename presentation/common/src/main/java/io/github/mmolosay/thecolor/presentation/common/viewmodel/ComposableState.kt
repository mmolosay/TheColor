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

/**
 * A single source of truth for one composition tree.
 */
class Store<T> private constructor(initial: T) {

    private val _state = MutableStateFlow(initial)
    val state: StateFlow<T> = _state.asStateFlow()

    private val writeMutex = Mutex()
    private var working: Maybe<T> = Maybe.None
    private val txId = Any()

    /**
     * Authoritative read: working copy inside a transaction, else committed.
     */
    internal suspend fun snapshot(): T =
        if (inMyTransaction()) {
            working.requireValue()
        } else {
            _state.value
        }

    /**
     * Fold into the active transaction, or commit atomically.
     */
    internal suspend fun submit(transform: (T) -> T) =
        if (inMyTransaction()) {
            val value = working.requireValue()
            val newValue = transform(value)
            working = Maybe.Some(newValue)
        } else {
            writeMutex.withLock {
                _state.update(transform)
            }
        }

    /**
     * Serializable transaction: the write lock is held for the entire block
     * (including suspending I/O inside it), and everything touched commits as
     * one emission at the end. Nested transactions on this store join the outer
     * one (no re-lock), so the non-reentrant Mutex never deadlocks.
     */
    internal suspend fun <R> transaction(block: suspend () -> R): R =
        if (inMyTransaction()) {
            block()
        } else {
            writeMutex.withLock {
                working = Maybe.Some(_state.value)
                try {
                    val newTxMarker = TxMarker(txId)
                    val result = withContext(newTxMarker) {
                        block()
                    }
                    _state.value = working.requireValue()
                    return@withLock result
                } finally {
                    working = Maybe.None
                }
            }
        }

    private class TxMarker(val id: Any) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<TxMarker>
    }

    private suspend fun inMyTransaction(): Boolean {
        val txMarker = currentCoroutineContext()[TxMarker.Key] ?: return false
        return (txMarker.id === txId)
    }
}

fun <T> Store<T>.rootStateHost(
    scope: CoroutineScope,
): StateHost<T> {
    return StateHostImpl(
        store = this,
        lens = Lens(
            get = { s -> s },
            set = { s, v -> v },
        ),
        scope = scope,
    )
}

interface StateHost<T> {
    val state: StateFlow<T>

    suspend fun current(): T
    suspend fun update(transform: (T) -> T)
    suspend fun <R> transaction(block: suspend () -> R): R

    fun <S> sub(inner: Lens<T, S>, scope: CoroutineScope): StateHost<S>
}

private class StateHostImpl<S, T>(
    private val store: Store<S>,
    private val lens: Lens<S, T>,
    private val scope: CoroutineScope,
) : StateHost<T> {

    override val state: StateFlow<T> =
        store.state
            .map { lens.get(it) }
            .stateIn(scope, SharingStarted.Eagerly, lens.get(store.state.value))

    override suspend fun current(): T =
        lens.get(source = store.snapshot())

    override suspend fun update(transform: (T) -> T) =
        store.submit { root ->
            val value = lens.get(root)
            val newValue = transform(value)
            lens.set(root, newValue)
        }

    override suspend fun <R> transaction(block: suspend () -> R): R =
        store.transaction(block)

    override fun <V> sub(
        inner: Lens<T, V>,
        scope: CoroutineScope,
    ): StateHost<V> =
        StateHostImpl(
            store = store,
            lens = lens.then(inner),
            scope = scope,
        )
}