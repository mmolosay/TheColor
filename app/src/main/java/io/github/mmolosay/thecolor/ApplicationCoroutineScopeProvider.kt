package io.github.mmolosay.thecolor

import kotlinx.coroutines.CoroutineScope

/**
 * Provides a [CoroutineScope] that is tied to a lifecycle of the whole application.
 */
interface ApplicationCoroutineScopeProvider {
    val applicationScope: CoroutineScope
}