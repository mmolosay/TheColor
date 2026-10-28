package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountConverter
import io.github.mmolosay.thecolor.demo.domain.AmountFormatter
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.NutritionFormatter
import io.github.mmolosay.thecolor.demo.domain.isSameAs

class NutritionCore @AssistedInject constructor(
    private val amountConverter: AmountConverter,
    private val amountFormatter: AmountFormatter,
    private val nutritionFormatter: NutritionFormatter,
) {

    // for no amount yet: the parent sets one with forAmountOf
    fun initialState(): NutritionState =
        NutritionState(request = null, result = null)

    // region Getters

    // the work the state awaits, derived from it
    fun NutritionState.awaited(): NutritionRequest? =
        if (this.result == null) this.request else null

    // The portion the facts suggest, unless it's the card's amount in another form. The view data
    // and selectSuggestion both go by it, so a suggestion shows only where taking it changes
    // something
    private fun NutritionState.suggestion(): Amount? {
        val facts = this.result?.getOrNull() ?: return null
        val isSame = context(amountConverter) { facts.suggestedAmount isSameAs facts.amount }
        return if (isSame) null else facts.suggestedAmount
    }

    fun NutritionState.toViewData(): NutritionViewData {
        val request = this.request ?: return NutritionViewData.NoAmount
        val portion = NutritionViewData.Portion(
            ingredient = request.ingredient.name,
            amount = amountFormatter.format(request.amount),
        )
        val result = this.result ?: return NutritionViewData.Loading(portion)
        val facts = result.getOrNull() ?: return NutritionViewData.Failed(portion)
        val suggestion = this.suggestion()?.let { amount ->
            NutritionViewData.Suggestion(
                amount = amount,
                label = amountFormatter.format(amount),
            )
        }
        return NutritionViewData.Ready(
            portion = portion,
            calories = nutritionFormatter.calories(facts.calories),
            protein = nutritionFormatter.protein(facts.protein),
            suggestion = suggestion,
        )
    }

    // endregion

    // region Setters

    // An operation for the parent: a new amount calls for new work, and the same amount, in any
    // form, keeps what it has
    fun NutritionState.forAmountOf(ingredient: Ingredient, amount: Amount?): NutritionState {
        if (amount == null) return NutritionState(request = null, result = null)
        val current = this.request
        if (current != null && current.ingredient == ingredient) {
            val isSame = context(amountConverter) { current.amount isSameAs amount }
            if (isSame) return this
        }
        val request = NutritionRequest(ingredient = ingredient, amount = amount)
        return NutritionState(request = request, result = null)
    }

    // for a parent: a card for facts it already has, which needs no work
    fun NutritionState.withFacts(facts: NutritionFacts): NutritionState {
        val request = NutritionRequest(ingredient = facts.ingredient, amount = facts.amount)
        return NutritionState(request = request, result = Result.success(facts))
    }

    // endregion

    // region Transitions

    // For its Instance only. They can run more than once, so they must have no side effects

    // applies only to the request the state still awaits
    fun NutritionState.completeFacts(
        result: Result<NutritionFacts>,
        expected: NutritionRequest,
    ): Change<NutritionState, Nothing> {
        if (this.awaited() != expected) return Change(this) // late, or already completed
        val state = this.copy(result = result)
        return Change(state)
    }

    // a new request for the same amount, after a failure
    fun NutritionState.retry(): Change<NutritionState, Nothing> {
        val failed = this.request
        if (failed == null || this.result?.isFailure != true) return Change(this)
        val request = NutritionRequest(ingredient = failed.ingredient, amount = failed.amount)
        val state = NutritionState(request = request, result = null)
        return Change(state)
    }

    // Applies only while the facts still suggest that amount, so that a tap on a suggestion from
    // facts that have changed since does nothing. The card moves to the suggested portion, and the
    // parent decides what else it means
    fun NutritionState.selectSuggestion(amount: Amount): Change<NutritionState, NutritionOutcome> {
        val request = this.request
        if (request == null || this.suggestion() != amount) return Change(this)
        val state = this.forAmountOf(request.ingredient, amount)
        return Change(state, outcome = NutritionOutcome.SuggestionSelected(amount))
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): NutritionCore
    }
}
