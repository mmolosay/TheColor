package io.github.mmolosay.thecolor.input

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge

/**
 * Combination of [SharingStarted.Eagerly] and [other] sharing strategy.
 */
class SharingStartedEagerlyAnd(
    private val other: SharingStarted,
) : SharingStarted {

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> =
        merge(
            SharingStarted.Eagerly.command(subscriptionCount),
            other.command(subscriptionCount),
        )
}