package io.github.mmolosay.thecolor.utils

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

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