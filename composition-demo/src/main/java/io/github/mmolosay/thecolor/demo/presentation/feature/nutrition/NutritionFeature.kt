package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class NutritionFeature @AssistedInject constructor(
    @Assisted lifetime: CoroutineScope,
    @Assisted private val store: Store<NutritionState, NutritionOutcome>,
    @Assisted private val environment: NutritionEnvironment,
    private val service: NutritionService, // from dependency injection: only the work uses it
) {

    val viewData: StateFlow<NutritionViewData> =
        store.mapState { nutrition -> context(environment) { nutrition.toViewData() } }

    init {
        // starts what the state awaits, and stops what it no longer awaits
        lifetime.launchAwaited(
            store = store,
            awaited = { nutrition -> context(environment) { nutrition.awaited() } },
            block = { request ->
                // a failure is a result too
                val result = service.nutritionFacts(request.ingredient, request.amount)
                store.update { nutrition ->
                    context(environment) { nutrition.receive(request, result) }
                }
            },
        )
    }

    fun retry() {
        store.update { nutrition ->
            context(environment) { nutrition.retry() }
        }
    }

    fun selectSuggestion(amount: Amount) {
        store.update { nutrition ->
            context(environment) { nutrition.selectSuggestion(amount) }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            lifetime: CoroutineScope,
            store: Store<NutritionState, NutritionOutcome>,
            environment: NutritionEnvironment,
        ): NutritionFeature
    }
}

// The transitions behind the actions and the results. Only the feature instance calls them. They're
// outside the class so that they can't reach its lifetime or store: they can run more than once, so
// they must have no side effects

// applies only to the request the state still awaits
context(_: NutritionEnvironment)
fun NutritionState.receive(
    request: NutritionRequest,
    result: Result<NutritionFacts>,
): Change<NutritionState, Nothing> {
    if (this.awaited() !== request) return Change(this) // late, or already received
    val state = this.copy(result = result)
    return Change(state)
}

// a new request for the same amount, after a failure
context(_: NutritionEnvironment)
fun NutritionState.retry(): Change<NutritionState, Nothing> {
    val failed = this.request
    if (failed == null || this.result?.isFailure != true) return Change(this)
    val request = NutritionRequest(ingredient = failed.ingredient, amount = failed.amount)
    val state = NutritionState(request = request, result = null)
    return Change(state)
}

// Applies only while the facts still suggest that amount, so that a tap on a suggestion from facts
// that have changed since does nothing. The card moves to the suggested portion, and the parent
// decides what else it means
context(environment: NutritionEnvironment)
fun NutritionState.selectSuggestion(amount: Amount): Change<NutritionState, NutritionOutcome> {
    val request = this.request
    if (request == null || this.suggestion() != amount) return Change(this)
    val state = this.forAmountOf(request.ingredient, amount)
    return Change(state, outcome = NutritionOutcome.SuggestionSelected(amount))
}
