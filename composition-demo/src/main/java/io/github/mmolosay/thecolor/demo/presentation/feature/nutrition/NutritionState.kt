package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.isSameAs

// compared by reference: a new request is new work, even for the same amount
class NutritionRequest(
    val ingredient: Ingredient,
    val amount: Amount,
)

// The nutrition facts for an amount of an ingredient, as they load. The request stays once its
// result arrives, as what the result is for
data class NutritionState(
    val request: NutritionRequest?, // the work for the amount the card is for; none without one
    val result: Result<NutritionFacts>?, // null while the request is awaited
)

sealed interface NutritionOutcome {
    // The user took the portion the facts suggest. The card has moved to it already, and the parent
    // decides what else it means
    data class SuggestionSelected(val amount: Amount) : NutritionOutcome
}

object NutritionStateFactory {

    // for no amount yet: the parent sets one with forAmountOf
    context(_: NutritionEnvironment)
    fun create(): NutritionState =
        NutritionState(request = null, result = null)
}

// the work the state awaits, derived from it
context(_: NutritionEnvironment)
fun NutritionState.awaited(): NutritionRequest? =
    if (this.result == null) this.request else null

// An operation for the parent: a new amount calls for new work, and the same amount, in any form,
// keeps what it has
context(environment: NutritionEnvironment)
fun NutritionState.forAmountOf(ingredient: Ingredient, amount: Amount?): NutritionState {
    if (amount == null) return NutritionState(request = null, result = null)
    val current = this.request
    if (current != null && current.ingredient == ingredient) {
        val isSame = context(environment.amountConverter) { current.amount isSameAs amount }
        if (isSame) return this
    }
    val request = NutritionRequest(ingredient = ingredient, amount = amount)
    return NutritionState(request = request, result = null)
}

// for a parent: a card for facts it already has, which needs no work
context(_: NutritionEnvironment)
fun NutritionState.withFacts(facts: NutritionFacts): NutritionState {
    val request = NutritionRequest(ingredient = facts.ingredient, amount = facts.amount)
    return NutritionState(request = request, result = Result.success(facts))
}

// The portion the facts suggest, unless it's the card's amount in another form. The view data and
// selectSuggestion both go by it, so a suggestion shows only where taking it changes something
context(environment: NutritionEnvironment)
fun NutritionState.suggestion(): Amount? {
    val facts = this.result?.getOrNull() ?: return null
    val isSame = context(environment.amountConverter) { facts.suggestedAmount isSameAs facts.amount }
    return if (isSame) null else facts.suggestedAmount
}

context(environment: NutritionEnvironment)
fun NutritionState.toViewData(): NutritionViewData {
    val request = this.request ?: return NutritionViewData.NoAmount
    val portion = NutritionViewData.Portion(
        ingredient = request.ingredient.name,
        amount = environment.amountFormatter.format(request.amount),
    )
    val result = this.result ?: return NutritionViewData.Loading(portion)
    val facts = result.getOrNull() ?: return NutritionViewData.Failed(portion)
    val suggestion = this.suggestion()?.let { amount ->
        NutritionViewData.Suggestion(
            amount = amount,
            label = environment.amountFormatter.format(amount),
        )
    }
    return NutritionViewData.Ready(
        portion = portion,
        calories = environment.nutritionFormatter.calories(facts.calories),
        protein = environment.nutritionFormatter.protein(facts.protein),
        suggestion = suggestion,
    )
}
