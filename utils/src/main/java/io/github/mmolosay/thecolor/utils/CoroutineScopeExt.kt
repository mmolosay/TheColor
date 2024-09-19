package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job

/**
 * Creates a [CoroutineScope] that is a "child" of a [parent] scope.
 *
 * This means that returned [CoroutineScope] is a new scope, but its lifecycle is tied to a
 * lifecycle of a [parent] scope according to Structured Concurrency rules.
 *
 * @param parent a parent scope.
 * @param job a producer of a [Job] to be used for returned [CoroutineScope] in its context.
 */
fun CoroutineScope(
    parent: CoroutineScope,
    job: (parentJob: Job) -> Job = { Job(it) },
): CoroutineScope {
    val parentJob = parent.coroutineContext.job
    val childJob = job(parentJob)
    val childContext = parent.coroutineContext + childJob
    return CoroutineScope(childContext)
}