package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Returns a [StateFlow] that holds [transform] applied to this flow's value.
 *
 * Its value is never behind this flow's, unlike that of a mapping made with `map` and `stateIn`, which lags
 * right after a change. A collector gets the current result first, then each result that differs
 * from the one before.
 *
 * The [transform] runs at every read of the value and for every collector, so it must be cheap,
 * and it must depend only on its argument; otherwise the value and what collectors get can differ.
 */
fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> =
    MappedStateFlow(
        source = this,
        transform = transform,
    )

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class MappedStateFlow<Source, Dest>(
    private val source: StateFlow<Source>,
    private val transform: (Source) -> Dest,
) : StateFlow<Dest> {

    override val value: Dest
        get() = transform(source.value)

    override val replayCache: List<Dest>
        get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<Dest>): Nothing {
        source
            .map(transform)
            .distinctUntilChanged()
            .collect(collector)
        error("StateFlow.collect() never completes")
    }
}