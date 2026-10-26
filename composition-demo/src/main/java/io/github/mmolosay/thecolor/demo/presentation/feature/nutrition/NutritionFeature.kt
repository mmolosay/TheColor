package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class NutritionFeature @AssistedInject constructor(
    @Assisted scope: CoroutineScope,
    @Assisted private val store: Store<NutritionState, NutritionOutcome>,
    @Assisted private val core: NutritionCore,
    service: NutritionService, // from dependency injection: only the work uses it
) {

    val viewData: StateFlow<NutritionViewData> =
        store.mapState { state -> with(core) { state.toViewData() } }

    init {
        // starts what the state awaits, and stops what it no longer awaits
        scope.launchAwaited(
            store = store,
            awaited = { state -> with(core) { state.awaited() } },
            block = { request ->
                // a failure is a result too
                val result = service.nutritionFacts(request.ingredient, request.amount)
                store.update { state -> with(core) { state.receive(request, result) } }
            },
        )
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
            scope: CoroutineScope,
            store: Store<NutritionState, NutritionOutcome>,
            core: NutritionCore,
        ): NutritionFeature
    }
}
