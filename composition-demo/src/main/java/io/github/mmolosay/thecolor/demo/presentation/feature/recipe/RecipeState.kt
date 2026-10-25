package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.domain.Sort
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorState
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.amount
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.hasPendingSubmission
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.version
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.withAmount
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.forAmountOf
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetState
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesState
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesStateFactory
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.forAmount
import io.github.mmolosay.thecolor.utils.Identity
import io.github.mmolosay.thecolor.utils.Lens

// The recipe's amount of flour, the nutrition card and the substitutes for it, and the sheet for a
// substitute
data class RecipeState(
    val editor: AmountEditorState,
    val nutrition: NutritionState,
    val substitutes: SubstitutesState,
    val sheet: SubstituteSheetState,
    val load: LoadRequest?, // the saved amount, to fill the editor with; see awaitedLoad
    val save: SaveRequest?, // the last whole amount submitted, to save; see awaitedSave
    val messages: List<SavedMessage>, // in the order the View shows them
)

object RecipeStateLenses {

    val editor = Lens<RecipeState, AmountEditorState>(
        get = { recipe -> recipe.editor },
        set = { recipe, editor -> recipe.copy(editor = editor) },
    )

    val nutrition = Lens<RecipeState, NutritionState>(
        get = { recipe -> recipe.nutrition },
        set = { recipe, nutrition -> recipe.copy(nutrition = nutrition) },
    )

    val substitutes = Lens<RecipeState, SubstitutesState>(
        get = { recipe -> recipe.substitutes },
        set = { recipe, substitutes -> recipe.copy(substitutes = substitutes) },
    )

    val sheet = Lens<RecipeState, SubstituteSheetState>(
        get = { recipe -> recipe.sheet },
        set = { recipe, sheet -> recipe.copy(sheet = sheet) },
    )
}

data class LoadRequest(
    val editorVersion: Int, // the editor's, when the load started
    val identity: Identity = Identity(),
)

data class SaveRequest(
    val amount: Amount,
    val identity: Identity = Identity(),
)

// "Saved 120 g"
data class SavedMessage(
    val amount: Amount,
    val identity: Identity = Identity(),
)

// a button on the screen that sets the amount and the substitutes' sort together
enum class RecipePreset(
    val amount: Amount,
    val sort: Sort,
) {
    CupByProtein(
        amount = Amount.Cups(whole = 1, numerator = 0, denominator = 1),
        sort = Sort.Protein,
    ),
    HalfCupByCalories(
        amount = Amount.Cups(whole = 0, numerator = 1, denominator = 2),
        sort = Sort.Calories,
    ),
}

object RecipeStateFactory {

    // with the editor empty, until the saved amount arrives
    context(environment: RecipeEnvironment)
    fun create(): RecipeState {
        val editor = context(environment.editor) { AmountEditorStateFactory.create(amount = null) }
        val nutrition = context(environment.nutrition) { NutritionStateFactory.create() }
        val substitutes = context(environment.substitutes) { SubstitutesStateFactory.create() }
        val sheet = context(environment.sheet) { SubstituteSheetStateFactory.create() }
        val state = RecipeState(
            editor = editor,
            nutrition = nutrition,
            substitutes = substitutes,
            sheet = sheet,
            load = run {
                val version = context(environment.editor) { editor.version() }
                LoadRequest(editorVersion = version)
            },
            save = null,
            messages = emptyList(),
        )
        return state.withCardsForAmount()
    }
}

// The agreement: the nutrition card and the substitutes are for the amount the editor stands for.
// Whatever changes the editor's amount calls it in the same update. The sheet isn't tied to it: it
// shows a substitute's portion, which stays true whatever the recipe's amount
context(environment: RecipeEnvironment)
fun RecipeState.withCardsForAmount(): RecipeState {
    val amount = context(environment.editor) { this.editor.amount() }
    val nutrition = context(environment.nutrition) {
        this.nutrition.forAmountOf(Ingredient.Flour, amount)
    }
    val substitutes = context(environment.substitutes) { this.substitutes.forAmount(amount) }
    return this.copy(nutrition = nutrition, substitutes = substitutes)
}

// the same fact, given: the editor takes the amount, as if the user had entered it, and the cards
// follow
context(environment: RecipeEnvironment)
fun RecipeState.withAmount(amount: Amount?): RecipeState {
    val editor = context(environment.editor) { this.editor.withAmount(amount) }
    return this.copy(editor = editor).withCardsForAmount()
}

// The load the screen waits for, derived from the state: only while nothing has set the editor since
// the load started. Whatever sets it first, the user or the screen, ends the wait, even if it's set
// back to what it held, and the work stops if the amount hasn't arrived yet
context(environment: RecipeEnvironment)
fun RecipeState.awaitedLoad(): LoadRequest? {
    val load = this.load ?: return null
    val version = context(environment.editor) { this.editor.version() }
    return load.takeIf { it.editorVersion == version }
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
            message = message,
            amount = environment.amountFormatter.format(message.amount),
        )
    }
    return RecipeViewData(
        isLoadingSavedAmount = (this.awaitedLoad() != null),
        messageToShow = messageToShow,
    )
}
