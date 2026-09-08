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
inline fun <T> MutableStateFlow<T>.batch(
    block: UpdateScope<T>.() -> Unit,
) {
    val scope = BatchUpdateScope<T>()
    with(scope) { block() }
    this.update { scope.apply(it) }
}

suspend inline fun <T> Store<T>.batch(
    block: UpdateScope<T>.() -> Unit,
) {
    val scope = BatchUpdateScope<T>()
    with(scope) { block() }
    this.update { scope.apply(it) }
}

fun <S, V> UpdateScope<S>.focus(lens: Lens<S, V>): UpdateScope<V> =
    FocusedUpdateScope(
        updateScope = this,
        lens = lens,
    )

private class FocusedUpdateScope<S, V>(
    private val updateScope: UpdateScope<S>,
    private val lens: Lens<S, V>,
) : UpdateScope<V> {
    override fun update(transform: (V) -> V) {
        updateScope.update { s ->
            val focused = lens.get(s)
            val value = transform(focused)
            lens.set(s, value)
        }
    }
}