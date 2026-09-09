package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.Job
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

/**
 * Runs [launch] against an isolated [BatchUpdateScope] and waits for the [Job] it returns.
 * Updates made in that scope are added to this one as a single update, or discarded if the job
 * was canceled — work that didn't finish never contributes.
 */
suspend fun <T> UpdateScope<T>.appendUnlessCancelled(
    launch: UpdateScope<T>.() -> Job,
) {
    val updates = BatchUpdateScope<T>()
    val job = with(updates, launch)
    job.join()
    if (job.isCancelled) return
    this.append(updates)
}

fun <T> UpdateScope<T>.append(batchScope: BatchUpdateScope<T>) =
    this.update(batchScope::apply)