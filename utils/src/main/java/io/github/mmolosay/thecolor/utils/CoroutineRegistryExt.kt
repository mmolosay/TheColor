package io.github.mmolosay.thecolor.utils

import io.github.mmolosay.thecolor.utils.CoroutineRegistry.Item
import io.github.mmolosay.thecolor.utils.CoroutineRegistryUtils.SupersededCancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

// region Extensions for CoroutineRegistry.AccessProvider

/**
 * Removes every item matching the [predicate] and returns the removed items.
 */
fun <T> CoroutineRegistry<T>.AccessProvider.removeAll(
    predicate: (Item<T>) -> Boolean,
): List<Item<T>> {
    val matching = this.items.filter(predicate)
    for (item in matching) {
        this.remove(item.job)
    }
    return matching
}

// endregion

// region Extensions for CoroutineRegistry

fun <T> CoroutineRegistry<T>.removeOnCompletion(job: Job): DisposableHandle =
    job.invokeOnCompletion {
        this.access { remove(job) }
    }

/**
 * Registers the [job] with the associated [value], first removing and canceling every item that
 * matches [predicate].
 * The removal and the addition are atomic with respect to each other.
 *
 * The superseded items are returned, but are not joined: the caller decides whether to wait for them or not.
 */
fun <T> CoroutineRegistry<T>.supersede(
    job: Job,
    value: T,
    predicate: (Item<T>) -> Boolean,
): List<Item<T>> {
    val superseded = this.access {
        val matching = this.removeAll(predicate)
        add(job, value)
        return@access matching
    }
    this.removeOnCompletion(job)
    for (item in superseded) {
        item.job.cancel(SupersededCancellationException(job, value))
    }
    return superseded
}

/**
 * Launches a coroutine with the [block] and [supersede]s it in the [registry] with the
 * associated [value].
 *
 * Guarantees that at any time there is at most one running [block] of the family selected by the [predicate]:
 * the [block] doesn't start executing until every superseded coroutine has completed canceling.
 * The launched coroutine is registered before it waits, so a concurrent call supersedes it correctly while it is still waiting.
 */
fun <T> CoroutineScope.launchSuperseding(
    context: CoroutineContext = EmptyCoroutineContext,
    registry: CoroutineRegistry<T>,
    value: T,
    predicate: (Item<T>) -> Boolean,
    block: suspend CoroutineScope.() -> Unit,
): Job {
    val superseded = CompletableDeferred<List<Job>>()
    val job = this.launch(context = context) {
        superseded.await().joinAll() // already canceled
        block()
    }
    run {
        val items = registry.supersede(job, value, predicate)
        val jobs = items.map { it.job }
        superseded.complete(jobs)
    }
    return job
}

// endregion

object CoroutineRegistryUtils {

    fun SupersededCancellationException(job: Job, value: Any?) =
        CancellationException(
            message = "Superseded by job=$job, value=$value",
            cause = null,
        )
}