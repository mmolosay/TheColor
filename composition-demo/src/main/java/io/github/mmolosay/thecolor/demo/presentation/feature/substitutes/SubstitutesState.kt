package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.Sort
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
