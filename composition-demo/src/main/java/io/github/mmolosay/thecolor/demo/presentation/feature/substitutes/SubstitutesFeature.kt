package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.demo.domain.Sort
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SubstitutesFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<SubstitutesState, SubstitutesOutcome>,
    @Assisted private val core: SubstitutesCore,
    service: NutritionService, // from dependency injection: only the work uses it
) {

    val viewData: StateFlow<SubstitutesViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        // starts what the state awaits, and stops what it no longer awaits
        scope.launchAwaited(
            store = store,
            awaited = { state -> with(core) { state.awaited() } },
            block = { request ->
                // a failure is a result too
                val result = service.substitutes(request.amount, request.sort)
                store.update { state -> with(core) { state.receive(request, result) } }
            },
        )
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
            scope: CoroutineScope,
            store: Store<SubstitutesState, SubstitutesOutcome>,
            core: SubstitutesCore,
        ): SubstitutesFeature
    }
}
