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

interface Store<T> {
    val flow: StateFlow<T>

    suspend fun current(): T
    suspend fun update(transform: (T) -> T)
    suspend fun <R> transaction(block: suspend () -> R): R

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
            .stateIn(scope, SharingStarted.Eagerly, lens.get(store.flow.value))

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