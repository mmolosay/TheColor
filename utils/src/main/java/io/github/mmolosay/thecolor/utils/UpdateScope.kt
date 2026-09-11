package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface UpdateScope<T> {
    fun update(transform: (T) -> T)
}

fun <T> MutableStateFlow<T>.asUpdateScope(): UpdateScope<T> =
    MutableStateFlowUpdateScope(delegate = this)

fun <S, V> MutableStateFlow<S>.asUpdateScope(lens: Lens<S, V>): UpdateScope<V> =
    this.asUpdateScope().focus(lens)

private class MutableStateFlowUpdateScope<T>(
    private val delegate: MutableStateFlow<T>,
) : UpdateScope<T> {
    override fun update(transform: (T) -> T) =
        delegate.update(transform)
}

@PublishedApi
internal class BatchUpdateScope<T> : UpdateScope<T> {
    private val updates = mutableListOf<(T) -> T>()
    private var isClosed = false

    override fun update(transform: (T) -> T) =
        synchronized(updates) {
            check(!isClosed) { "This batch has closed" }
            updates += transform
        }

    fun close() =
        synchronized(updates) {
            isClosed = true
        }

    fun apply(value: T): T =
        updates.fold(initial = value) { acc, update ->
            update(acc)
        }
}

inline fun <T, R> MutableStateFlow<T>.batch(
    block: UpdateScope<T>.() -> R,
): R {
    val scope = BatchUpdateScope<T>()
    val result = try {
        with(scope, block)
    } finally {
        scope.close()
    }
    this.update { scope.apply(it) }
    return result
}

fun <S, V> UpdateScope<S>.focus(lens: Lens<S, V>): UpdateScope<V> =
    FocusedUpdateScope(
        delegate = this,
        lens = lens,
    )

inline fun <S, V> UpdateScope<S>.focus(
    lens: Lens<S, V>,
    block: UpdateScope<V>.() -> Unit,
) =
    this.focus(lens).run(block)

private class FocusedUpdateScope<S, V>(
    private val delegate: UpdateScope<S>,
    private val lens: Lens<S, V>,
) : UpdateScope<V> {
    override fun update(transform: (V) -> V) {
        delegate.update { s ->
            lens.modify(s, transform)
        }
    }
}

/**
 * Returns an [UpdateScope] over the non-null value of this scope.
 * Updates made in the returned scope are dropped while the value is `null`.
 */
fun <T : Any> UpdateScope<T?>.dropOnNull(): UpdateScope<T> =
    DropOnNullUpdateScope(delegate = this)

private class DropOnNullUpdateScope<T : Any>(
    private val delegate: UpdateScope<T?>,
) : UpdateScope<T> {
    override fun update(transform: (T) -> T) {
        delegate.update { s ->
            if (s == null) return@update s
            transform(s)
        }
    }
}