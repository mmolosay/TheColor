package io.github.mmolosay.thecolor.utils.composition

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object CompositionUtils {

    fun <S, R : Any> CoroutineScope.serve(
        store: Store<S, *>,
        request: (S) -> R?,
        block: suspend (R) -> Unit,
    ): Job =
        this.launch {
            store
                .map { state -> request(state) }
                .distinctUntilChanged()
                .collectLatest {
                    if (it != null) block(it)
                }
        }
}