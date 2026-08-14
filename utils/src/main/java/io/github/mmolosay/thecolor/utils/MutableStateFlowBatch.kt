package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class BatchScope<T> {
    private val _updates = mutableListOf<(T) -> T>()
    val updates: List<(T) -> T>
        get() = _updates.toList()

    fun update(transform: (T) -> T) {
        _updates += transform
    }

    fun apply(value: T): T =
        updates.fold(initial = value) { acc, update ->
            update(acc)
        }
}

inline fun <T> MutableStateFlow<T>.batch(
    block: BatchScope<T>.() -> Unit,
) {
    val scope = BatchScope<T>()
    with(scope) { block() }
    this.update { scope.apply(it) }
}

suspend inline fun <T> Store<T>.batch(
    block: BatchScope<T>.() -> Unit,
) {
    val scope = BatchScope<T>()
    with(scope) { block() }
    this.update { scope.apply(it) }
}