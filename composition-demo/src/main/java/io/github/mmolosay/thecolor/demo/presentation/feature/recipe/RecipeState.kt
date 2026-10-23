package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import io.github.mmolosay.thecolor.composition.Token
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorState
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.hasPendingSubmission
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.forAmountOf

// The recipe's amount of flour, and the nutrition card for it
data class RecipeState(
    val editor: AmountEditorState,
    val nutrition: NutritionState,
    val load: LoadRequest?, // the saved amount, to fill the editor with; none once it arrives
    val save: SaveRequest?, // the last whole amount submitted, to save; see awaitedSave
    val messages: List<SavedMessage>, // in the order the View shows them
)

// compared by reference: loading the saved amount, as the screen opens
class LoadRequest

// compared by reference: a new submission is new work, even for the same amount
class SaveRequest(
    val amount: Amount,
)

// "Saved 120 g", shown once; a new token for each, so each one is shown once
data class SavedMessage(
    val token: Token,
    val amount: Amount,
)

object RecipeStateFactory {

    // with the editor empty, until the saved amount arrives
    context(environment: RecipeEnvironment)
    fun create(): RecipeState {
        val editor = context(environment.editor) { AmountEditorStateFactory.create(amount = null) }
        val nutrition = context(environment.nutrition) { NutritionStateFactory.create() }
        val state = RecipeState(
            editor = editor,
            nutrition = nutrition,
            load = LoadRequest(),
            save = null,
            messages = emptyList(),
        )
        return state.withNutritionForAmount()
    }
}

// The agreement: the nutrition card is for the amount the editor stands for. Whatever changes the
// editor's amount calls it in the same update
context(environment: RecipeEnvironment)
fun RecipeState.withNutritionForAmount(): RecipeState {
    val amount = context(environment.editor) { this.editor.amount() }
    val nutrition = context(environment.nutrition) {
        this.nutrition.forAmountOf(Ingredient.Flour, amount)
    }
    return this.copy(nutrition = nutrition)
}

// The save the screen waits for, derived from the state: only while its submission waits for an
// answer. An edit that withdraws the submission leaves the save nothing to answer, so the wait ends,
// and the work stops if it hasn't saved yet
context(environment: RecipeEnvironment)
fun RecipeState.awaitedSave(): SaveRequest? {
    val isPending = context(environment.editor) { this.editor.hasPendingSubmission() }
    return if (isPending) this.save else null
}

context(environment: RecipeEnvironment)
fun RecipeState.toViewData(): RecipeViewData {
    val messageToShow = run {
        val message = this.messages.firstOrNull() ?: return@run null
        RecipeViewData.MessageToShow(
            token = message.token,
            amount = environment.amountFormatter.format(message.amount),
        )
    }
    return RecipeViewData(
        isLoadingSavedAmount = (this.load != null),
        messageToShow = messageToShow,
    )
}
