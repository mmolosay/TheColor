package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

// no editor or card: their Views show them, from their own data
data class RecipeViewData(
    val isLoadingSavedAmount: Boolean,
    val messageToShow: MessageToShow?, // the oldest message the View hasn't shown yet
) {
    data class MessageToShow(
        val message: SavedMessage, // which the View acknowledges
        val amount: String, // "120 g"
    )
}
