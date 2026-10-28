package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.CupSampler
import io.github.mmolosay.thecolor.main.di.qualifiers.CoroutineDispatcherDiQualifiers.DefaultDispatcher
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class CupDialFeature @AssistedInject constructor(
    @Assisted private val core: CupDialCore,
    @Assisted private val store: Store<CupDialState, *>,
    @Assisted private val scope: CoroutineScope,
    // from dependency injection: only the work uses them
    private val sampler: CupSampler,
    @DefaultDispatcher private val workDispatcher: CoroutineDispatcher,
) {

    val viewData: StateFlow<CupDialViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        serveSample()
    }

    // starts what the state awaits, and stops what it no longer awaits
    private fun serveSample() {
        scope.serve(
            store = store,
            request = { state -> with(core) { state.awaited() } },
        ) { request ->
            // off the main thread, as sampling a color area would be; the Views collect on the main
            // thread either way
            withContext(workDispatcher) {
                val amount = sampler.amountAt(request.position)
                store.update { state -> with(core) { state.completeSample(amount, request) } }
            }
        }
    }

    fun drag(position: Float) {
        store.update { state -> with(core) { state.drag(position) } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: CupDialCore,
            store: Store<CupDialState, *>,
            scope: CoroutineScope,
        ): CupDialFeature
    }
}
