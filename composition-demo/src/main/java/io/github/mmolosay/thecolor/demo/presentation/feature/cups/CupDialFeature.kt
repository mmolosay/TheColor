package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.CupSampler
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class CupDialFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<CupDialState, *>,
    @Assisted private val core: CupDialCore,
    // from dependency injection: only the work uses them
    sampler: CupSampler,
    @DefaultDispatcher workDispatcher: CoroutineDispatcher,
) {

    val viewData: StateFlow<CupDialViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        // starts what the state awaits, and stops what it no longer awaits
        scope.launchAwaited(
            store = store,
            awaited = { state -> with(core) { state.awaited() } },
            block = { request ->
                // off the main thread, as sampling a color area would be; the Views collect on the
                // main thread either way
                withContext(workDispatcher) {
                    val amount = sampler.amountAt(request.position)
                    store.update { state -> with(core) { state.receiveSample(request, amount) } }
                }
            },
        )
    }

    fun drag(position: Float) {
        store.update { state -> with(core) { state.drag(position) } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            scope: CoroutineScope,
            store: Store<CupDialState, *>,
            core: CupDialCore,
        ): CupDialFeature
    }
}
