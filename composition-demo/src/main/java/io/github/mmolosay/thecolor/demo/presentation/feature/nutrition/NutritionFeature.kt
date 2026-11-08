package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.serve
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class NutritionFeature @AssistedInject constructor(
    @Assisted private val core: NutritionCore,
    @Assisted private val store: Store<NutritionState, NutritionOutcome>,
    @Assisted private val scope: CoroutineScope,
    private val service: NutritionService, // from dependency injection: only the work uses it
) {

    val viewData: StateFlow<NutritionViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        serveFacts()
    }

    // starts what the state awaits, and stops what it no longer awaits
    private fun serveFacts() {
        scope.serve(
            store = store,
            request = { state -> with(core) { state.awaited() } },
        ) { request ->
            // a failure is a result too
            val result = service.nutritionFacts(request.ingredient, request.amount)
            store.update { state -> with(core) { state.completeFacts(result, request) } }
        }
    }

    fun retry() {
        store.update { state -> with(core) { state.retry() } }
    }

    fun selectSuggestion(amount: Amount) {
        store.update { state -> with(core) { state.selectSuggestion(amount) } }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            core: NutritionCore,
            store: Store<NutritionState, NutritionOutcome>,
            scope: CoroutineScope,
        ): NutritionFeature
    }
}
