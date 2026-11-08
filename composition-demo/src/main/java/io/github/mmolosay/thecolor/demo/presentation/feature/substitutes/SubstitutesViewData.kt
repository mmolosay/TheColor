package io.github.mmolosay.thecolor.demo.presentation.feature.substitutes

import io.github.mmolosay.thecolor.demo.domain.NutritionFacts
import io.github.mmolosay.thecolor.demo.domain.Sort

sealed interface SubstitutesViewData {

    data object NoAmount : SubstitutesViewData

    data class Loading(
        val sort: Sort,
    ) : SubstitutesViewData

    data class Ready(
        val sort: Sort,
        val items: List<Item>,
    ) : SubstitutesViewData

    data class Failed(
        val sort: Sort,
    ) : SubstitutesViewData

    data class Item(
        val facts: NutritionFacts, // what the View passes back when the user picks it
        val name: String, // "Oat flour"
        val calories: String, // "485 kcal"
        val protein: String, // "17.6 g protein"
    )
}
