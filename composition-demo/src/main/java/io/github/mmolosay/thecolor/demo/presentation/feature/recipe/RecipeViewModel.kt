package io.github.mmolosay.thecolor.demo.presentation.feature.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.composition.CompositionUtils.launchAwaited
import io.github.mmolosay.thecolor.composition.Store
import io.github.mmolosay.thecolor.composition.Token
import io.github.mmolosay.thecolor.composition.viewData
import io.github.mmolosay.thecolor.demo.domain.Amount
import io.github.mmolosay.thecolor.demo.domain.RecipeRepository
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.AmountEditorState
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.isEmpty
import io.github.mmolosay.thecolor.demo.presentation.feature.editor.withSubmissionAccepted
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.nutrition.NutritionState
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.SubstituteSheetState
import io.github.mmolosay.thecolor.demo.presentation.feature.sheet.withSubstitute
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesOutcome
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.SubstitutesState
import io.github.mmolosay.thecolor.demo.presentation.feature.substitutes.withSort
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldFeature
import io.github.mmolosay.thecolor.demo.presentation.feature.textfield.TextFieldStateFactory
import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val environment: RecipeEnvironment,
    editorFactory: AmountEditorFeature.Factory,
    nutritionFactory: NutritionFeature.Factory,
    substitutesFactory: SubstitutesFeature.Factory,
    sheetFactory: SubstituteSheetFeature.Factory,
    textFieldFactory: TextFieldFeature.Factory,
    private val repository: RecipeRepository, // from dependency injection: only the work uses it
) : ViewModel() {

    private val store = run {
        val state = context(environment) { RecipeStateFactory.create() }
        Store(initialState = state)
    }

    val viewData: StateFlow<RecipeViewData> =
        store.viewData { recipe -> context(environment) { recipe.toViewData() } }

    // for its View, which shows the children's Views
    val editor: AmountEditorFeature = run {
        val slice = store.slice<AmountEditorState, AmountEditorOutcome>(editorLens) { recipe, outcome ->
            context(environment) { onEditor(recipe, outcome) }
        }
        editorFactory.create(
            lifetime = viewModelScope,
            store = slice,
            environment = environment.editor,
        )
    }

    val nutrition: NutritionFeature = run {
        val slice = store.slice<NutritionState, NutritionOutcome>(nutritionLens) { recipe, outcome ->
            context(environment) { onNutrition(recipe, outcome) }
        }
        nutritionFactory.create(
            lifetime = viewModelScope,
            store = slice,
            environment = environment.nutrition,
        )
    }

    val substitutes: SubstitutesFeature = run {
        val slice = store.slice<SubstitutesState, SubstitutesOutcome>(substitutesLens) {
                recipe, outcome ->
            context(environment) { onSubstitutes(recipe, outcome) }
        }
        substitutesFactory.create(
            lifetime = viewModelScope,
            store = slice,
            environment = environment.substitutes,
        )
    }

    val sheet: SubstituteSheetFeature = run {
        // nothing to decide: the sheet has no outcomes, and the screen opens it in onSubstitutes
        val slice = store.slice(sheetLens) { recipe, _: Nothing? -> Change(recipe) }
        sheetFactory.create(
            lifetime = viewModelScope,
            store = slice,
            environment = environment.sheet,
        )
    }

    // Nothing in the screen's functions reads or writes the servings, so the field gets a store of its
    // own, and its commits call only its own View
    val servings: TextFieldFeature = run {
        val state = context(environment.servings) { TextFieldStateFactory.create(text = "1") }
        textFieldFactory.create(
            lifetime = viewModelScope,
            store = Store(initialState = state),
            environment = environment.servings,
        )
    }

    init {
        // each kind of work on its own, so that one never restarts the other
        viewModelScope.launchAwaited(
            store = store,
            awaited = { recipe -> recipe.load },
            block = { request ->
                val amount = repository.lastAmount()
                store.update { recipe ->
                    context(environment) { recipe.receiveLoad(request, amount) }
                }
            },
        )
        viewModelScope.launchAwaited(
            store = store,
            awaited = { recipe -> context(environment) { recipe.awaitedSave() } },
            block = { request ->
                repository.saveLastAmount(request.amount)
                store.update { recipe ->
                    context(environment) { recipe.receiveSave(request) }
                }
            },
        )
    }

    fun applyPreset(preset: RecipePreset) {
        store.update { recipe ->
            context(environment) { recipe.applyPreset(preset) }
        }
    }

    fun messageShown(token: Token) {
        store.update { recipe ->
            context(environment) { recipe.messageShown(token) }
        }
    }
}

private val editorLens = Lens<RecipeState, AmountEditorState>(
    get = { recipe -> recipe.editor },
    set = { recipe, editor -> recipe.copy(editor = editor) },
)

private val nutritionLens = Lens<RecipeState, NutritionState>(
    get = { recipe -> recipe.nutrition },
    set = { recipe, nutrition -> recipe.copy(nutrition = nutrition) },
)

private val substitutesLens = Lens<RecipeState, SubstitutesState>(
    get = { recipe -> recipe.substitutes },
    set = { recipe, substitutes -> recipe.copy(substitutes = substitutes) },
)

private val sheetLens = Lens<RecipeState, SubstituteSheetState>(
    get = { recipe -> recipe.sheet },
    set = { recipe, sheet -> recipe.copy(sheet = sheet) },
)

// The reducers for its children. Only the screen's ViewModel runs them, inside a child's update

// For any change of the editor's own, the user's or not. The cards follow the amount the change
// leaves. A submitted amount is saved, and its submission waits until it is
context(_: RecipeEnvironment)
fun onEditor(
    recipe: RecipeState,
    outcome: AmountEditorOutcome?,
): Change<RecipeState, Nothing> {
    val state = recipe.withCardsForAmount()
    return when (outcome) {
        null -> Change(state)
        is AmountEditorOutcome.Submitted -> {
            val save = SaveRequest(amount = outcome.amount)
            Change(state.copy(save = save))
        }
    }
}

// a suggested portion taken on the card becomes the recipe's amount, as if the user had entered it
context(_: RecipeEnvironment)
fun onNutrition(
    recipe: RecipeState,
    outcome: NutritionOutcome?,
): Change<RecipeState, Nothing> =
    when (outcome) {
        null -> Change(recipe)
        is NutritionOutcome.SuggestionSelected -> Change(recipe.withAmount(outcome.amount))
    }

// a substitute picked from the list opens in the sheet, as a new session
context(environment: RecipeEnvironment)
fun onSubstitutes(
    recipe: RecipeState,
    outcome: SubstitutesOutcome?,
): Change<RecipeState, Nothing> =
    when (outcome) {
        null -> Change(recipe)
        is SubstitutesOutcome.SubstituteSelected -> {
            val sheet = context(environment.sheet) { recipe.sheet.withSubstitute(outcome.facts) }
            Change(recipe.copy(sheet = sheet))
        }
    }

// The transitions behind the actions and the results. Only the screen's ViewModel calls them.
// They're outside the class so that they can't reach its lifetime or store: they can run more than
// once, so they must have no side effects

// the amount and the sort change as one: the editor never holds the preset's amount while the
// substitutes are in the old sort
context(environment: RecipeEnvironment)
fun RecipeState.applyPreset(preset: RecipePreset): Change<RecipeState, Nothing> {
    val withAmount = this.withAmount(preset.amount)
    val substitutes = context(environment.substitutes) {
        withAmount.substitutes.withSort(preset.sort)
    }
    val state = withAmount.copy(substitutes = substitutes)
    return Change(state)
}

// The saved amount fills only the empty editor the screen opened with, so that it never replaces
// what the user typed meanwhile. The editor is the expected value: what the editor holds tells, not
// which changes it went through, since some of them aren't the user's, such as a setting arriving
context(environment: RecipeEnvironment)
fun RecipeState.receiveLoad(
    request: LoadRequest,
    amount: Amount?,
): Change<RecipeState, Nothing> {
    if (this.load !== request) return Change(this) // already received
    val isEmpty = context(environment.editor) { this.editor.isEmpty() }
    if (!isEmpty) return Change(this.copy(load = null)) // the user has typed meanwhile
    val state = this.copy(load = null).withAmount(amount)
    return Change(state)
}

// Applies only to the save the screen still waits for. Accepts the submission it was made for, and
// asks for the message, in one change, so that neither happens without the other
context(environment: RecipeEnvironment)
fun RecipeState.receiveSave(request: SaveRequest): Change<RecipeState, Nothing> {
    if (this.awaitedSave() !== request) return Change(this) // withdrawn, or a newer one is saved
    val editor = context(environment.editor) { this.editor.withSubmissionAccepted() }
    val message = SavedMessage(token = Token(), amount = request.amount)
    val state = this.copy(editor = editor, save = null, messages = this.messages + message)
    return Change(state)
}

// what's left once the View has shown the message for this token: a newer message stays
context(_: RecipeEnvironment)
fun RecipeState.messageShown(token: Token): Change<RecipeState, Nothing> {
    val messages = this.messages.filterNot { message -> message.token === token }
    val state = this.copy(messages = messages)
    return Change(state)
}
