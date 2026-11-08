package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.demo.domain.Sort
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SubstitutesFeature @AssistedInject constructor(
    @Assisted private val core: SubstitutesCore,
    @Assisted private val store: Store<SubstitutesState, SubstitutesOutcome>,
    @Assisted private val scope: CoroutineScope,
    private val service: NutritionService, // from dependency injection: only the work uses it
) {

    val viewData: StateFlow<SubstitutesViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        serveSubstitutes()
    }

    // starts what the state awaits, and stops what it no longer awaits
    private fun serveSubstitutes() {
        scope.serve(
            store = store,
            request = { state -> with(core) { state.awaited() } },
        ) { request ->
            // a failure is a result too
            val result = service.substitutes(request.amount, request.sort)
            store.update { state -> with(core) { state.completeSubstitutes(result, request) } }
        }
    }

    fun retry() {
        store.update { state -> with(core) { state.retry() } }
    }

    fun select(facts: NutritionFacts) {
        store.update { state -> with(core) { state.select(facts) } }
    }

    fun sortBy(sort: Sort) {
        store.update { state -> with(core) { state.sortBy(sort) } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: SubstitutesCore,
            store: Store<SubstitutesState, SubstitutesOutcome>,
            scope: CoroutineScope,
        ): SubstitutesFeature
    }
}
