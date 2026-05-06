package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.CoroutineRegistry.Item
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// region Extensions for CoroutineRegistry

suspend fun <T> CoroutineRegistry<T>.items(): List<Item<T>> =
    this.access {
        items
    }

suspend fun <T> CoroutineRegistry<T>.add(job: Job, value: T): Item<T> =
    this.access {
        add(job = job, value = value)
    }

suspend fun <T> CoroutineRegistry<T>.remove(job: Job): Item<T>? =
    this.access {
        remove(job)
    }

context(coroutineScope: CoroutineScope)
suspend fun <T> CoroutineRegistry<T>.removeThis(): Item<T>? =
    this.access {
        remove(job = coroutineScope.coroutineContext.job)
    }

@Suppress("unused")
suspend fun <T> CoroutineRegistry<T>.removeAndCancel(job: Job): Item<T>? =
    this.access {
        removeAndCancel(job)
    }

@Suppress("unused")
suspend fun <T> CoroutineRegistry<T>.requireRemove(job: Job): Item<T> {
    val removed = this.remove(job)
    require(removed != null) { "The job $job was not found in the items" }
    return removed
}

/**
 * Wraps the execution of the coroutine [block] within an [add]-[remove] lifecycle.
 *
 * If the exception occurs inside the [block] (including coroutine's [CancellationException]),
 * then the [job] will still be [remove]d from the registry correctly, but the function will re-throw the exception.
 *
 * If so happens that by the time the [block] has finished executing and the [job] has already
 * been removed by some other code, then no exception will be thrown and the function will return gracefully.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T, R> CoroutineRegistry<T>.track(
    job: Job,
    value: T,
    block: () -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    this.add(job, value)
    try {
        return block()
    } finally {
        this.remove(job)
    }
}

/**
 * [Tracks][CoroutineRegistry.track] the [Job] of the current [CoroutineScope].
 * Supposed to be used inside the launched coroutine to [track] this coroutine.
 */
@OptIn(ExperimentalContracts::class)
context(coroutineScope: CoroutineScope)
suspend inline fun <T, R> CoroutineRegistry<T>.trackThis(
    value: T,
    block: () -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return this.track(
        job = coroutineScope.coroutineContext.job,
        value = value,
        block = block,
    )
}

/**
 * Guarantees that at any time there will be at most only one __active__ [track]ed [Job]
 * of the type specified by the [predicate].
 * All present items that match the [predicate] will be [removed and canceled][removeAndCancelAll].
 * However, there can be more than one [block] running at the same time.
 * See [Job]'s `cancelling` state.
 */
@OptIn(ExperimentalContracts::class)
context(coroutineScope: CoroutineScope)
suspend inline fun <T, R> CoroutineRegistry<T>.trackThisAsSingleActive(
    crossinline predicate: (Item<T>) -> Boolean,
    value: T,
    block: () -> R,
): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val job = coroutineScope.coroutineContext.job
    this.access {
        removeAndCancelAll(predicate = predicate)
        add(job = job, value = value)
    }
    try {
        return block()
    } finally {
        this.remove(job)
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
fun <T> CoroutineRegistry<T>.AccessProvider.addThis(value: T): Item<T> =
    this.add(job = coroutineScope.coroutineContext.job, value = value)

fun <T> CoroutineRegistry<T>.AccessProvider.removeAndCancel(job: Job): Item<T>? =
    this.remove(job)?.also { item ->
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
        removeAndCancel(item.job)
    }
    return items
}

fun <T> CoroutineRegistry<T>.AccessProvider.removeAndCancelAll(): List<Item<T>> =
    this.removeAndCancelAll { true }

// endregion