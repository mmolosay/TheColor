package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.CoroutineRegistry.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// region Extensions for CoroutineRegistry

suspend fun <T> CoroutineRegistry<T>.items(): List<Item<T>> =
    this.access {
        items
    }

suspend fun <T> CoroutineRegistry<T>.add(value: T, job: Job): Item.Id =
    this.access {
        add(value = value, job = job)
    }

suspend fun <T> CoroutineRegistry<T>.remove(id: Item.Id): Item<T>? =
    this.access {
        remove(id = id)
    }

@Suppress("unused")
suspend fun <T> CoroutineRegistry<T>.removeAndCancel(id: Item.Id): Item<T>? =
    this.access {
        removeAndCancel(id)
    }

@Suppress("unused")
suspend fun <T> CoroutineRegistry<T>.requireRemove(id: Item.Id): Item<T> {
    val removed = this.remove(id)
    require(removed != null) { "The value with ID=$id was not found in the items" }
    return removed
}

/**
 * Wraps the execution of the [block], which is supposed to process the [value],
 * within an [add]-[remove] lifecycle.
 *
 * If the exception occurs inside the [block] (including coroutine's [CancellationException]),
 * then the [value] will still be [remove]d from the registry correctly, but the function will re-throw the exception.
 *
 * If so happens that by the time the [block] has finished executing and the [value] has already
 * been removed by some other code, then no exception will be thrown and the function will return gracefully.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T, R> CoroutineRegistry<T>.track(
    value: T,
    job: Job,
    block: () -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val id = this.add(value, job)
    try {
        return block()
    } finally {
        this.remove(id)
    }
}

@OptIn(ExperimentalContracts::class)
context(coroutineScope: CoroutineScope)
suspend inline fun <T, R> CoroutineRegistry<T>.track(
    value: T,
    block: () -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return this.track(
        value = value,
        job = coroutineScope.coroutineContext.job,
        block = block,
    )
}

/**
 * Wraps the execution of the [block], which is supposed to process the [value],
 * within an [add]-[remove] lifecycle.
 *
 * Guarantees that at any time there will be at most only one active [Job] that corresponds to the
 * specified [value].
 * However, there can be more than one [block] running at the same time.
 * See [Job]'s `cancelling` state.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T, R> CoroutineRegistry<T>.trackSingleActive(
    crossinline removeAndCancelAll: (Item<T>) -> Boolean,
    value: T,
    block: () -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val id = this.access {
        removeAndCancelAll(predicate = removeAndCancelAll)
        add(value = value, job = currentCoroutineContext().job)
    }
    try {
        return block()
    } finally {
        this.remove(id)
    }
}

suspend inline fun <T> CoroutineRegistry<T>.removeAndCancelAll(): List<Item<T>> =
    this.removeAndCancelAll { true }

suspend inline fun <T> CoroutineRegistry<T>.removeAndCancelAll(
    crossinline predicate: (Item<T>) -> Boolean,
): List<Item<T>> =
    this.access {
        removeAndCancelAll(predicate)
    }

// endregion

// region Extensions for CoroutineRegistry.AccessProvider

context(coroutineScope: CoroutineScope)
fun <T> CoroutineRegistry<T>.AccessProvider.add(value: T): Item.Id =
    this.add(value = value, job = coroutineScope.coroutineContext.job)

fun <T> CoroutineRegistry<T>.AccessProvider.removeAndCancel(id: Item.Id): Item<T>? =
    this.remove(id = id)?.also { item ->
        item.job.cancel()
    }

/**
 * [Removes][CoroutineRegistry.remove] and [cancels][Job.cancel] all items matching the given [predicate].
 *
 * @return the list of removed items.
 */
inline fun <T> CoroutineRegistry<T>.AccessProvider.removeAndCancelAll(
    crossinline predicate: (Item<T>) -> Boolean,
): List<Item<T>> {
    val items = this.items.filter(predicate)
    for (item in items) {
        removeAndCancel(item.id)
    }
    return items
}

// endregion