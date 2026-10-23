package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionService
import io.github.mmolosay.thecolor.demo.domain.Sort
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class SubstitutesFeature @AssistedInject constructor(
    @Assisted lifetime: CoroutineScope,
    @Assisted private val store: Store<SubstitutesState, SubstitutesOutcome>,
    @Assisted private val environment: SubstitutesEnvironment,
    private val service: NutritionService, // from dependency injection: only the work uses it
) {

    val viewData: StateFlow<SubstitutesViewData> =
        store.mapState { substitutes -> context(environment) { substitutes.toViewData() } }

    init {
        // starts what the state awaits, and stops what it no longer awaits
        lifetime.launchAwaited(
            store = store,
            awaited = { substitutes -> context(environment) { substitutes.awaited() } },
            block = { request ->
                // a failure is a result too
                val result = service.substitutes(request.amount, request.sort)
                store.update { substitutes ->
                    context(environment) { substitutes.receive(request, result) }
                }
            },
        )
    }

    fun sortBy(sort: Sort) {
        store.update { substitutes ->
            context(environment) { substitutes.sortBy(sort) }
        }
    }

    fun select(facts: NutritionFacts) {
        store.update { substitutes ->
            context(environment) { substitutes.select(facts) }
        }
    }

    fun retry() {
        store.update { substitutes ->
            context(environment) { substitutes.retry() }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(
            lifetime: CoroutineScope,
            store: Store<SubstitutesState, SubstitutesOutcome>,
            environment: SubstitutesEnvironment,
        ): SubstitutesFeature
    }
}

// The transitions behind the actions and the results. Only the feature instance calls them. They're
// outside the class so that they can't reach its lifetime or store: they can run more than once, so
// they must have no side effects

// applies only to the request the state still awaits
context(_: SubstitutesEnvironment)
fun SubstitutesState.receive(
    request: SubstitutesRequest,
    result: Result<List<NutritionFacts>>,
): Change<SubstitutesState, SubstitutesOutcome> {
    if (this.awaited() !== request) return Change(this) // late, or already received
    val state = this.copy(result = result)
    return Change(state)
}

context(_: SubstitutesEnvironment)
fun SubstitutesState.sortBy(sort: Sort): Change<SubstitutesState, SubstitutesOutcome> {
    val state = this.withSort(sort)
    return Change(state)
}

// applies only while the list still holds that substitute: one picked from a list replaced since
// does nothing
context(_: SubstitutesEnvironment)
fun SubstitutesState.select(facts: NutritionFacts): Change<SubstitutesState, SubstitutesOutcome> {
    if (!this.holds(facts)) return Change(this)
    return Change(this, outcome = SubstitutesOutcome.SubstituteSelected(facts))
}

// a new request for the same amount and sort, after a failure
context(_: SubstitutesEnvironment)
fun SubstitutesState.retry(): Change<SubstitutesState, SubstitutesOutcome> {
    val failed = this.request
    if (failed == null || this.result?.isFailure != true) return Change(this)
    val request = SubstitutesRequest(amount = failed.amount, sort = failed.sort)
    val state = SubstitutesState(request = request, result = null)
    return Change(state)
}
