package io.github.mmolosay.thecolor.input

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge

/**
 * Combination of [SharingStarted.Eagerly] and [other] sharing strategy.
 *
 * We need to start eagerly so flows of color input ViewModels can pre-compute
 * their first emission. This way when UI subscribes to these flows, already emitted value
 * is replayed to them, so UI doesn't wait for a data transformation for even a moment.
 *
 * Will probably be gone once TODO: BasicTextField2 migration
 * is done.
 */
internal class SharingStartedEagerlyAnd(
    private val other: SharingStarted,
) : SharingStarted {

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> =
        merge(
            SharingStarted.Eagerly.command(subscriptionCount),
            other.command(subscriptionCount),
        )
}