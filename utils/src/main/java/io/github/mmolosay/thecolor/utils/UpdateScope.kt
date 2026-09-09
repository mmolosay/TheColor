package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

interface UpdateScope<T> {
    fun update(transform: (T) -> T)
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

@Suppress("unused") // useful, independent util that may come handy in future
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
fun <T : Any> UpdateScope<T?>.focusNotNull(): UpdateScope<T> =
    NotNullUpdateScope(delegate = this)

private class NotNullUpdateScope<T : Any>(
    private val delegate: UpdateScope<T?>,
) : UpdateScope<T> {
    override fun update(transform: (T) -> T) {
        delegate.update { s ->
            if (s == null) return@update s
            transform(s)
        }
    }
}