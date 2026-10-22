package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorState
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.forAmountOf

// The recipe's amount of flour, and the nutrition card for it
data class RecipeState(
    val editor: AmountEditorState,
    val nutrition: NutritionState,
)

object RecipeStateFactory {

    context(environment: RecipeEnvironment)
    fun create(): RecipeState {
        val editor = context(environment.editor) { AmountEditorStateFactory.create(amount = null) }
        val nutrition = context(environment.nutrition) { NutritionStateFactory.create() }
        val state = RecipeState(editor = editor, nutrition = nutrition)
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
