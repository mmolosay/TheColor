package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.utils.Identity

data class NutritionRequest(
    val ingredient: Ingredient,
    val amount: Amount,
    val identity: Identity = Identity(),
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
