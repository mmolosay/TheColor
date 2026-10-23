package io.github.mmolosay.thecolor.presentation.common.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

// How every View reads its view data: the newest when the screen starts, then each new one while
// it's started. A collector reads the current value whenever it runs, so the View never goes back,
// and Compose runs it before the next frame is drawn, so the View has an action's change by then
@Composable
fun <V> StateFlow<V>.collectAsViewData(): State<V> =
    this.collectAsStateWithLifecycle()
