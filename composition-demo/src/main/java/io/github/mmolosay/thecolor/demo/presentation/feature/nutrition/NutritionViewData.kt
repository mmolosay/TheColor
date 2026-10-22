package io.github.mmolosay.thecolor.demo.presentation.feature.nutrition

sealed interface NutritionViewData {

    data object NoAmount : NutritionViewData

    data class Loading(
        val portion: Portion,
    ) : NutritionViewData

    data class Ready(
        val portion: Portion,
        val calories: String, // "437 kcal"
        val protein: String, // "12.4 g protein"
    ) : NutritionViewData

    data class Failed(
        val portion: Portion,
    ) : NutritionViewData

    // what the facts are for
    data class Portion(
        val ingredient: String, // "Flour"
        val amount: String, // "120 g"
    )
}
