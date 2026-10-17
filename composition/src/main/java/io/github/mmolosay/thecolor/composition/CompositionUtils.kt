package io.github.mmolosay.thecolor.composition

import io.github.mmolosay.thecolor.utils.CoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object CompositionUtils {

    fun <S, R : Any> CoroutineScope.launchAwaited(
        store: Store<S>,
        awaited: (S) -> R?,
        block: suspend (R) -> Unit,
    ): Job =
        this.launch {
            store
                .map { moment -> awaited(moment.state) }
                .distinctUntilChanged { old, new -> old === new }
                .collectLatest {
                    if (it != null) block(it)
                }
        }

    fun CoroutineScope.child(): CoroutineScope =
        CoroutineScope(parent = this, job = { SupervisorJob(it) })
}