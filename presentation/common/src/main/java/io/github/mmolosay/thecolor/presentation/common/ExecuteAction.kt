package io.github.mmolosay.thecolor.presentation.common

import kotlinx.coroutines.Job

/**
 * Executes an `Action` of a feature.
 *
 * Returns the coroutine the action started, or `null` if it started none.
 * Neither says whether the action had an effect: a started coroutine may still drop it.
 */
typealias ExecuteAction<A> = (action: A) -> Job?