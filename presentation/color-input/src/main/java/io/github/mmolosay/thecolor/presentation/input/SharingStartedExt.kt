package io.github.mmolosay.thecolor.presentation.input

import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.merge

/**
 * Combines [SharingCommand]s from two sharing strategies.
 *
 * We need to start eagerly so flows of 'Color Input' ViewModels can pre-compute
 * their first emission. This way when UI subscribes to these flows, already emitted value
 * is replayed to them, so UI doesn't wait for a data transformation for even a moment.
 *
 * This function isn't extracted to `util` module to be accessible from other modules,
 * because it wasn't extensively tested.
 *
 * Will probably be gone once TODO: BasicTextField2 migration
 * is done.
 */
internal operator fun SharingStarted.plus(other: SharingStarted): SharingStarted =
    SharingStarted { subscriptionCount ->
        merge(
            this.command(subscriptionCount),
            other.command(subscriptionCount),
        )
    }