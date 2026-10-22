package io.github.mmolosay.thecolor.presentation.common.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LifecycleStartEffect
import io.github.mmolosay.thecolor.composition.ViewDataSource

// The listener is the only thing that writes what the View shows, and it always writes the newest
// view data, on the main thread: inside every commit made there, so a View has its own writes before
// its action returns, and after every other commit in a later task. So the View never goes back, and
// catches up once the state stops changing. Since a commit made on the main thread writes here at
// once, nothing changes a store during composition, as Compose asks of any side effect
@Composable
fun <V> ViewDataSource<V>.collectAsViewData(): State<V> {
    val source = this
    // an equal view data isn't written, so it recomposes nothing
    val state = remember(source) { mutableStateOf(source.value) }
    LifecycleStartEffect(source) {
        state.value = source.value // the newest, in case it changed while the screen was stopped
        val listening = source.listen { state.value = source.value }
        onStopOrDispose {
            listening.close()
        }
    }
    return state
}
