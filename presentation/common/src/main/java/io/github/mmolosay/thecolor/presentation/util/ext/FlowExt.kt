package io.github.mmolosay.thecolor.presentation.util.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

/**
 * Converts [Flow] in [SharedFlow] with `replay` parameter equal to __zero__,
 * so it doesn't re-emit last values for new collectors.
 *
 * It is useful, when you want to observe some command-like `Flow`, but it
 * should not be collected again on UI lifecycle restart, when old collectors
 * reattach themselves to `this` `Flow`.
 *
 * Can be useful for displaying single-time messages (toasts and snackbars),
 * different types of commands and so on.
 *
 * @see Flow.shareIn
 */
fun <T> Flow<T>.shareOnceIn(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.Eagerly
): SharedFlow<T> =
    this.shareIn(scope, started, replay = 0)