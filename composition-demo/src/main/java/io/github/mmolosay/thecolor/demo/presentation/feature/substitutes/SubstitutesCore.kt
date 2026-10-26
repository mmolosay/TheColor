package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionFormatter
import io.github.mmolosay.thecolor.demo.domain.Sort
import io.github.mmolosay.thecolor.demo.domain.isSameAs

class SubstitutesCore @AssistedInject constructor(
    private val amountConverter: AmountConverter,
    private val nutritionFormatter: NutritionFormatter,
) {

    // for no amount yet: the parent sets one with forAmount
    fun initialState(): SubstitutesState =
        SubstitutesState(request = null, result = null)

    // region Getters

    // the work the state awaits, derived from it
    fun SubstitutesState.awaited(): SubstitutesRequest? =
        if (this.result == null) this.request else null

    // whether the list holds this substitute, as the View showed it
    private fun SubstitutesState.holds(facts: NutritionFacts): Boolean {
        val substitutes = this.result?.getOrNull() ?: return false
        return (facts in substitutes)
    }

    fun SubstitutesState.toViewData(): SubstitutesViewData {
        val request = this.request ?: return SubstitutesViewData.NoAmount
        val result = this.result ?: return SubstitutesViewData.Loading(sort = request.sort)
        val substitutes = result.getOrNull()
            ?: return SubstitutesViewData.Failed(sort = request.sort)
        val items = substitutes.map { facts ->
            SubstitutesViewData.Item(
                facts = facts,
                name = facts.ingredient.name,
                calories = nutritionFormatter.calories(facts.calories),
                protein = nutritionFormatter.protein(facts.protein),
            )
        }
        return SubstitutesViewData.Ready(sort = request.sort, items = items)
    }

    // endregion

    // region Setters

    // An operation for the parent: a new amount calls for new work in the same sort, and the same
    // amount, in any form, keeps what it has
    fun SubstitutesState.forAmount(amount: Amount?): SubstitutesState {
        if (amount == null) return SubstitutesState(request = null, result = null)
        val current = this.request
        if (current != null) {
            val isSame = context(amountConverter) { current.amount isSameAs amount }
            if (isSame) return this
        }
        val sort = current?.sort ?: Sort.Calories
        val request = SubstitutesRequest(amount = amount, sort = sort)
        return SubstitutesState(request = request, result = null)
    }

    // for the parent, and for the user's choice: the same amount in another order; nothing without
    // one
    fun SubstitutesState.withSort(sort: Sort): SubstitutesState {
        val current = this.request ?: return this
        if (current.sort == sort) return this
        val request = SubstitutesRequest(amount = current.amount, sort = sort)
        return SubstitutesState(request = request, result = null)
    }

    // endregion

    // region Transitions

    // For its Instance only. They can run more than once, so they must have no side effects

    // applies only to the request the state still awaits
    fun SubstitutesState.receive(
        request: SubstitutesRequest,
        result: Result<List<NutritionFacts>>,
    ): Change<SubstitutesState, SubstitutesOutcome> {
        if (this.awaited() != request) return Change(this) // late, or already received
        val state = this.copy(result = result)
        return Change(state)
    }

    // a new request for the same amount and sort, after a failure
    fun SubstitutesState.retry(): Change<SubstitutesState, SubstitutesOutcome> {
        val failed = this.request
        if (failed == null || this.result?.isFailure != true) return Change(this)
        val request = SubstitutesRequest(amount = failed.amount, sort = failed.sort)
        val state = SubstitutesState(request = request, result = null)
        return Change(state)
    }

    // applies only while the list still holds that substitute: one picked from a list replaced
    // since does nothing
    fun SubstitutesState.select(
        facts: NutritionFacts,
    ): Change<SubstitutesState, SubstitutesOutcome> {
        if (!this.holds(facts)) return Change(this)
        return Change(this, outcome = SubstitutesOutcome.SubstituteSelected(facts))
    }

    fun SubstitutesState.sortBy(sort: Sort): Change<SubstitutesState, SubstitutesOutcome> {
        val state = this.withSort(sort)
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): SubstitutesCore
    }
}
