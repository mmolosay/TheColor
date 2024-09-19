package io.github.mmolosay.thecolor.presentation.api

import io.github.mmolosay.thecolor.utils.CoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

/**
 * Creates a [CoroutineScope] for a [SimpleViewModel] based on [parent] scope
 * (presumably a scope of an Android-aware, "real" `ViewModel`).
 *
 * Default [job] produces [SupervisorJob], which is commonly preferred way to organize
 * coroutines inside ViewModels.
 *
 * @see io.github.mmolosay.thecolor.utils.CoroutineScope
 */
fun ViewModelCoroutineScope(
    parent: CoroutineScope,
    job: (parentJob: Job) -> Job = { SupervisorJob(it) },
): CoroutineScope =
    CoroutineScope(
        parent = parent,
        job = job,
    )