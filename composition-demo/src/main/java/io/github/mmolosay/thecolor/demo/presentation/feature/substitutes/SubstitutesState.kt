package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.Sort
import io.github.mmolosay.thecolor.demo.domain.isSameAs
import io.github.mmolosay.thecolor.utils.Identity

data class SubstitutesRequest(
    val amount: Amount,
    val sort: Sort,
    val identity: Identity = Identity(),
)

// The best substitutes for flour for an amount, as they load. The sort belongs to the request:
// without an amount, there's nothing to sort
data class SubstitutesState(
    val request: SubstitutesRequest?, // the work for the amount and the sort; none without an amount
    val result: Result<List<NutritionFacts>>?, // null while the request is awaited
)

sealed interface SubstitutesOutcome {
    // the user picked a substitute from the list; the parent decides what it means
    data class SubstituteSelected(val facts: NutritionFacts) : SubstitutesOutcome
}

object SubstitutesStateFactory {

    // for no amount yet: the parent sets one with forAmount
    context(_: SubstitutesEnvironment)
    fun create(): SubstitutesState =
        SubstitutesState(request = null, result = null)
}

// the work the state awaits, derived from it
context(_: SubstitutesEnvironment)
fun SubstitutesState.awaited(): SubstitutesRequest? =
    if (this.result == null) this.request else null

// An operation for the parent: a new amount calls for new work in the same sort, and the same amount,
// in any form, keeps what it has
context(environment: SubstitutesEnvironment)
fun SubstitutesState.forAmount(amount: Amount?): SubstitutesState {
    if (amount == null) return SubstitutesState(request = null, result = null)
    val current = this.request
    if (current != null) {
        val isSame = context(environment.amountConverter) { current.amount isSameAs amount }
        if (isSame) return this
    }
    val sort = current?.sort ?: Sort.Calories
    val request = SubstitutesRequest(amount = amount, sort = sort)
    return SubstitutesState(request = request, result = null)
}

// for the parent, and for the user's choice: the same amount in another order; nothing without one
context(_: SubstitutesEnvironment)
fun SubstitutesState.withSort(sort: Sort): SubstitutesState {
    val current = this.request ?: return this
    if (current.sort == sort) return this
    val request = SubstitutesRequest(amount = current.amount, sort = sort)
    return SubstitutesState(request = request, result = null)
}

// whether the list holds this substitute, as the View showed it
context(_: SubstitutesEnvironment)
fun SubstitutesState.holds(facts: NutritionFacts): Boolean {
    val substitutes = this.result?.getOrNull() ?: return false
    return (facts in substitutes)
}

context(environment: SubstitutesEnvironment)
fun SubstitutesState.toViewData(): SubstitutesViewData {
    val request = this.request ?: return SubstitutesViewData.NoAmount
    val result = this.result ?: return SubstitutesViewData.Loading(sort = request.sort)
    val substitutes = result.getOrNull() ?: return SubstitutesViewData.Failed(sort = request.sort)
    val items = substitutes.map { facts ->
        SubstitutesViewData.Item(
            facts = facts,
            name = facts.ingredient.name,
            calories = environment.nutritionFormatter.calories(facts.calories),
            protein = environment.nutritionFormatter.protein(facts.protein),
        )
    }
    return SubstitutesViewData.Ready(sort = request.sort, items = items)
}
