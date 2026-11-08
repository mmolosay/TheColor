package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.AmountFormatter
import io.github.mmolosay.thecolor.demo.domain.Ingredient
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorCore
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionCore
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetCore
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesCore
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldCore
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFormatter

class RecipeCore @AssistedInject constructor(
    private val amountFormatter: AmountFormatter,
    editorFactory: AmountEditorCore.Factory,
    nutritionFactory: NutritionCore.Factory,
    substitutesFactory: SubstitutesCore.Factory,
    sheetFactory: SubstituteSheetCore.Factory,
    textFieldFactory: TextFieldCore.Factory,
) {

    // its children's, one each
    val editorCore: AmountEditorCore = editorFactory.create()
    val nutritionCore: NutritionCore = nutritionFactory.create()
    val substitutesCore: SubstitutesCore = substitutesFactory.create()
    val sheetCore: SubstituteSheetCore = sheetFactory.create()

    // the servings' Text Field's, with the rule for the text it allows
    val servingsCore: TextFieldCore = textFieldFactory.create(formatter = servingsTextFormatter)

    // with the editor empty, until the saved amount arrives
    fun initialState(): RecipeState {
        val editor = editorCore.initialState(amount = null)
        val state = RecipeState(
            editor = editor,
            nutrition = nutritionCore.initialState(),
            substitutes = substitutesCore.initialState(),
            sheet = sheetCore.initialState(),
            load = LoadRequest(editorVersion = with(editorCore) { editor.version() }),
            save = null,
            messages = emptyList(),
        )
        return state.withCardsForAmount()
    }

    // region Getters

    // The load the screen waits for, derived from the state: only while nothing has set the editor
    // since the load started. Whatever sets it first, the user or the screen, ends the wait, even
    // if it's set back to what it held, and the work stops if the amount hasn't arrived yet
    fun RecipeState.awaitedLoad(): LoadRequest? {
        val load = this.load ?: return null
        val version = with(editorCore) { editor.version() }
        return load.takeIf { it.editorVersion == version }
    }

    // The save the screen waits for, derived from the state: only while its submission waits for an
    // answer. An edit that withdraws the submission leaves the save nothing to answer, so the wait
    // ends, and the work stops if it hasn't saved yet
    fun RecipeState.awaitedSave(): SaveRequest? {
        val isPending = with(editorCore) { editor.hasPendingSubmission() }
        return if (isPending) this.save else null
    }

    fun RecipeState.toViewData(): RecipeViewData {
        val messageToShow = run {
            val message = this.messages.firstOrNull() ?: return@run null
            RecipeViewData.MessageToShow(
                message = message,
                amount = amountFormatter.format(message.amount),
            )
        }
        return RecipeViewData(
            isLoadingSavedAmount = (this.awaitedLoad() != null),
            messageToShow = messageToShow,
        )
    }

    // endregion

    // region Setters

    // the same fact, given: the editor takes the amount, as if the user had entered it, and the
    // cards follow
    private fun RecipeState.withAmount(amount: Amount?): RecipeState {
        val agreed = with(editorCore) { editor.withAmount(amount) }
        return this.copy(editor = agreed).withCardsForAmount()
    }

    // The agreement: the nutrition card and the substitutes are for the amount the editor stands
    // for. Whatever changes the editor's amount calls it in the same update. The sheet isn't tied
    // to it: it's for a substitute's portion, which stays true whatever the recipe's amount
    private fun RecipeState.withCardsForAmount(): RecipeState {
        val amount = with(editorCore) { editor.amount() }
        return this.copy(
            nutrition = with(nutritionCore) { nutrition.forAmountOf(Ingredient.Flour, amount) },
            substitutes = with(substitutesCore) { substitutes.forAmount(amount) },
        )
    }

    // endregion

    // region Transitions

    // For the screen's ViewModel only, which runs each reducer inside its child's update. They
    // can run more than once, so they must have no side effects

    // For any change of the editor's own, the user's or not. The cards follow the amount the change
    // leaves. A submitted amount is saved, and its submission waits until it is
    fun RecipeState.onEditor(outcome: AmountEditorOutcome?): Change<RecipeState, Nothing> {
        val state = this.withCardsForAmount()
        return when (outcome) {
            null -> Change(state)
            is AmountEditorOutcome.Submitted -> {
                val save = SaveRequest(amount = outcome.amount)
                Change(state.copy(save = save))
            }
        }
    }

    // a suggested portion taken on the card becomes the recipe's amount, as if the user had entered
    // it
    fun RecipeState.onNutrition(outcome: NutritionOutcome?): Change<RecipeState, Nothing> =
        when (outcome) {
            null -> Change(this)
            is NutritionOutcome.SuggestionSelected -> Change(this.withAmount(outcome.amount))
        }

    // a substitute picked from the list opens in the sheet, as a new session
    fun RecipeState.onSubstitutes(outcome: SubstitutesOutcome?): Change<RecipeState, Nothing> =
        when (outcome) {
            null -> Change(this)
            is SubstitutesOutcome.SubstituteSelected -> {
                val opened = with(sheetCore) { sheet.withSubstitute(outcome.facts) }
                Change(this.copy(sheet = opened))
            }
        }

    // what's left once the View has acknowledged this message: a newer one stays
    fun RecipeState.acknowledgeMessage(message: SavedMessage): Change<RecipeState, Nothing> {
        val messages = this.messages.filterNot { it == message }
        val state = this.copy(messages = messages)
        return Change(state)
    }

    // the amount and the sort change as one: the editor never holds the preset's amount while the
    // substitutes are in the old sort
    fun RecipeState.applyPreset(preset: RecipePreset): Change<RecipeState, Nothing> {
        val withAmount = this.withAmount(preset.amount)
        val sorted = with(substitutesCore) { withAmount.substitutes.withSort(preset.sort) }
        val state = withAmount.copy(substitutes = sorted)
        return Change(state)
    }

    // Applies only to the load the screen still waits for, so that the saved amount fills only the
    // editor as the screen opened it, and never replaces what was entered meanwhile, even if it was
    // emptied since
    fun RecipeState.completeLoad(
        amount: Amount?,
        expected: LoadRequest,
    ): Change<RecipeState, Nothing> {
        // completed, or the editor set since
        if (this.awaitedLoad() != expected) return Change(this)
        val state = this.copy(load = null).withAmount(amount)
        return Change(state)
    }

    // Applies only to the save the screen still waits for. Accepts the submission it was made for,
    // and asks for the message, in one change, so that neither happens without the other
    fun RecipeState.completeSave(expected: SaveRequest): Change<RecipeState, Nothing> {
        // withdrawn, or a newer one is saved
        if (this.awaitedSave() != expected) return Change(this)
        val accepted = with(editorCore) { editor.withSubmissionAccepted() }
        val message = SavedMessage(amount = expected.amount)
        val state = this.copy(editor = accepted, save = null, messages = this.messages + message)
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): RecipeCore
    }
}

// keeps only the text it allows: a number of servings from 1 to 99, or nothing while it's typed
private val servingsTextFormatter = TextFormatter { text ->
    text.filter { it in '0'..'9' }.trimStart('0').take(2)
}
