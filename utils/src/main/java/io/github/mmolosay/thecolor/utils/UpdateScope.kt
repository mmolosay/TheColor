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

class BatchUpdateScope<T> : UpdateScope<T> {
    private val _updates = mutableListOf<(T) -> T>() // TODO: not safe for concurrency. Address.
    val updates: List<(T) -> T>
        get() = _updates.toList()

    override fun update(transform: (T) -> T) {
        _updates += transform
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
    val result = with(scope) { block() }
    this.update { scope.apply(it) }
    return result
}

suspend inline fun <T, R> Store<T>.batch(
    block: UpdateScope<T>.() -> R,
): R {
    val scope = BatchUpdateScope<T>()
    val result = with(scope) { block() }
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